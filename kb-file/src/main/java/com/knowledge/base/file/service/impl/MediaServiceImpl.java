package com.knowledge.base.file.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledge.base.file.config.FileStorageProperties;
import com.knowledge.base.file.entity.FileInfo;
import com.knowledge.base.file.mapper.FileMapper;
import com.knowledge.base.file.service.MediaService;
import com.knowledge.base.file.storage.FileStorage;
import com.knowledge.base.file.storage.FileStorageFactory;
import com.knowledge.base.file.vo.MediaMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {
    private final FileMapper fileMapper;
    private final FileStorageFactory storageFactory;
    private final FileStorageProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public MediaMetadata probeMediaInfo(Long fileId) {
        FileInfo file = require(fileId); Path source = null;
        try {
            source = downloadToTemp(file); String output = command(List.of(properties.getFfmpeg().getFfprobePath(), "-v", "error", "-print_format", "json", "-show_format", "-show_streams", source.toString()));
            MediaMetadata metadata = parse(output); updateMetadata(fileId, metadata); return metadata;
        } catch (Exception exception) { log.warn("Media probe failed for file {}", fileId, exception); return null; }
        finally { delete(source); }
    }

    @Override
    public String transcodeToHls(Long fileId) {
        FileInfo file = require(fileId); Path source = null; Path output = null;
        try {
            source = downloadToTemp(file); output = Files.createTempDirectory("kb-hls-");
            if ("VIDEO".equals(file.getFileType())) {
                transcodeVideo(source, output.resolve("360p"), 640, 360, 28, "64k");
                transcodeVideo(source, output.resolve("720p"), 1280, 720, 23, "128k");
                Files.writeString(output.resolve("master.m3u8"), videoMasterPlaylist());
            } else {
                transcodeAudio(source, output.resolve("audio"));
                Files.writeString(output.resolve("master.m3u8"), audioMasterPlaylist());
            }
            String hlsPath = "media/" + fileId + "/hls"; uploadDirectory(output, hlsPath);
            FileInfo update = new FileInfo(); update.setId(fileId); update.setHlsPath(hlsPath); fileMapper.updateById(update); return hlsPath;
        } catch (Exception exception) { log.error("HLS transcoding failed for file {}", fileId, exception); return null; }
        finally { delete(source); deleteDirectory(output); }
    }

    @Override
    public String generateThumbnail(Long fileId) {
        FileInfo file = require(fileId); Path source = null; Path thumbnail = null;
        try {
            source = downloadToTemp(file); thumbnail = Files.createTempFile("kb-thumb-", ".jpg");
            command(List.of(properties.getFfmpeg().getPath(), "-ss", String.valueOf(properties.getFfmpeg().getThumbnailTime()), "-i", source.toString(), "-frames:v", "1", "-q:v", "2", "-y", thumbnail.toString()));
            String path = "media/" + fileId + "/thumbnail.jpg"; upload(path, thumbnail); FileInfo update = new FileInfo(); update.setId(fileId); update.setThumbnailPath(path); fileMapper.updateById(update); return path;
        } catch (Exception exception) { log.warn("Thumbnail generation failed for file {}", fileId, exception); return null; }
        finally { delete(source); delete(thumbnail); }
    }

    @Override
    public void updateTranscodeStatus(Long fileId, String status) { FileInfo update = new FileInfo(); update.setId(fileId); update.setTranscodeStatus(status); fileMapper.updateById(update); }

    private void transcodeVideo(Path source, Path directory, int width, int height, int crf, String audioBitrate) throws IOException, InterruptedException {
        Files.createDirectories(directory); Path playlist = directory.resolve("index.m3u8");
        command(List.of(properties.getFfmpeg().getPath(), "-i", source.toString(), "-c:v", "libx264", "-preset", "fast", "-crf", String.valueOf(crf), "-vf", "scale=" + width + ":" + height, "-c:a", "aac", "-b:a", audioBitrate, "-hls_time", String.valueOf(properties.getFfmpeg().getHlsSegmentTime()), "-hls_list_size", "0", "-hls_segment_filename", directory.resolve("%03d.ts").toString(), "-y", playlist.toString()));
    }

    private void transcodeAudio(Path source, Path directory) throws IOException, InterruptedException {
        Files.createDirectories(directory);
        Path playlist = directory.resolve("index.m3u8");
        command(List.of(properties.getFfmpeg().getPath(), "-i", source.toString(), "-vn", "-c:a", "aac", "-b:a", "128k", "-hls_time", String.valueOf(properties.getFfmpeg().getHlsSegmentTime()), "-hls_list_size", "0", "-hls_segment_filename", directory.resolve("%03d.ts").toString(), "-y", playlist.toString()));
    }

    private String command(List<String> values) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(values).redirectErrorStream(true).start(); boolean finished = process.waitFor(properties.getFfmpeg().getTimeoutSeconds(), TimeUnit.SECONDS); String output = new String(process.getInputStream().readAllBytes());
        if (!finished) { process.destroyForcibly(); throw new IOException("FFmpeg process timed out"); }
        if (process.exitValue() != 0) throw new IOException("FFmpeg process failed: " + output);
        return output;
    }

    private MediaMetadata parse(String json) throws IOException {
        JsonNode root = objectMapper.readTree(json); JsonNode format = root.path("format"); Integer duration = numeric(format.path("duration"), 1); Integer bitrate = numeric(format.path("bit_rate"), 1000); String videoCodec = null, audioCodec = null, resolution = null;
        for (JsonNode stream : root.path("streams")) { if ("video".equals(stream.path("codec_type").asText())) { videoCodec = stream.path("codec_name").asText(null); int width = stream.path("width").asInt(); int height = stream.path("height").asInt(); if (width > 0 && height > 0) resolution = width + "x" + height; } else if ("audio".equals(stream.path("codec_type").asText())) audioCodec = stream.path("codec_name").asText(null); }
        return MediaMetadata.builder().duration(duration).bitrate(bitrate).resolution(resolution).videoCodec(videoCodec).audioCodec(audioCodec).build();
    }

    private Integer numeric(JsonNode value, int divisor) { try { return value.isMissingNode() || value.isNull() ? null : (int) Math.round(Double.parseDouble(value.asText()) / divisor); } catch (NumberFormatException ignored) { return null; } }
    private void updateMetadata(Long id, MediaMetadata value) { if (value == null) return; FileInfo update = new FileInfo(); update.setId(id); update.setDuration(value.getDuration()); update.setResolution(value.getResolution()); update.setBitrate(value.getBitrate()); fileMapper.updateById(update); }
    private FileInfo require(Long id) { FileInfo file = fileMapper.selectById(id); if (file == null) throw new IllegalArgumentException("File not found: " + id); return file; }
    private Path downloadToTemp(FileInfo file) throws IOException { String suffix = file.getOriginalName() == null || !file.getOriginalName().contains(".") ? ".media" : file.getOriginalName().substring(file.getOriginalName().lastIndexOf('.')); Path target = Files.createTempFile("kb-media-", suffix); try (InputStream input = storage().getInputStream(file.getFilePath())) { Files.copy(input, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING); } return target; }
    private void uploadDirectory(Path root, String base) throws IOException { try (Stream<Path> paths = Files.walk(root)) { for (Path path : paths.filter(Files::isRegularFile).toList()) upload(base + "/" + root.relativize(path).toString().replace('\\', '/'), path); } }
    private void upload(String relativePath, Path source) throws IOException { try (InputStream input = Files.newInputStream(source)) { if (!storage().upload(input, relativePath, Files.size(source))) throw new IOException("Failed to upload media output"); } }
    private FileStorage storage() { return storageFactory.getStorage(); }
    private String videoMasterPlaylist() { return "#EXTM3U\n#EXT-X-VERSION:3\n#EXT-X-STREAM-INF:BANDWIDTH=600000,RESOLUTION=640x360\n360p/index.m3u8\n#EXT-X-STREAM-INF:BANDWIDTH=1500000,RESOLUTION=1280x720\n720p/index.m3u8\n"; }
    private String audioMasterPlaylist() { return "#EXTM3U\n#EXT-X-VERSION:3\n#EXT-X-STREAM-INF:BANDWIDTH=128000,CODECS=\"mp4a.40.2\"\naudio/index.m3u8\n"; }
    private void delete(Path path) { if (path != null) try { Files.deleteIfExists(path); } catch (IOException ignored) { } }
    private void deleteDirectory(Path path) { if (path != null) try (Stream<Path> values = Files.walk(path)) { values.sorted(java.util.Comparator.reverseOrder()).forEach(this::delete); } catch (IOException ignored) { } }
}
