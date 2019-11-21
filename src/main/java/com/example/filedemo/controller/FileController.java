package com.example.filedemo.controller;

import com.example.filedemo.payload.UploadFileResponse;
import com.example.filedemo.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.activation.FileTypeMap;
import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Map;

@RestController
public class FileController {

	private static final Logger logger = LoggerFactory.getLogger(FileController.class);

	@Autowired
	private FileStorageService fileStorageService;

	@PostMapping("/uploadFile")
	public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
		String fileName = fileStorageService.storeFile(file);

		String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/downloadFile/")
				.path(fileName).toUriString();

		String filePreviewUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/preview/").path(fileName)
				.toUriString();

		return new UploadFileResponse(fileName, fileDownloadUri, filePreviewUri, file.getContentType(), file.getSize());
	}

	@PostMapping("/uploadMultipleFiles")
	public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
		return Arrays.asList(files).stream().map(file -> uploadFile(file)).collect(Collectors.toList());
	}

	@GetMapping("/downloadFile/{fileName:.+}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
		// Load file as Resource
		Resource resource = fileStorageService.loadFileAsResource(fileName);

		// Try to determine file's content type
		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (IOException ex) {
			logger.info("Could not determine file type.");
		}

		// Fallback to the default content type if type could not be determined
		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}

	/**
	 * Show images to get from local storage to web page
	 */
	@GetMapping("/preview/{fileName}")
	public ResponseEntity<byte[]> previewFile(@PathVariable String fileName) throws IOException {

		Resource resource = fileStorageService.loadFileAsResource(fileName);
		
		File img = resource.getFile();

		// Return readed image
		return ResponseEntity.ok()
				.contentType(MediaType.valueOf(FileTypeMap.getDefaultFileTypeMap().getContentType(img)))
				.body(Files.readAllBytes(img.toPath()));
	}

	/**
	 * List all image files in image server.
	 * 
	 */
	@GetMapping("/listImages")
	public ResponseEntity<Object> getImageList() {
		try (Stream<Path> walk = Files.walk(this.fileStorageService.getFileStorageLocation())) {
			List<Map<String, String>> mapList = walk.filter(Files::isRegularFile)
					.map(x -> new HashMap<String, String>() {
						{
							put("name", x.getFileName().toString());
							put("preview", ServletUriComponentsBuilder.fromCurrentContextPath().path("/preview/")
									.path(x.getFileName().toString()).toUriString());
						}
					}).collect(Collectors.toList());
			return ResponseEntity.ok().body(mapList);
		} catch (NoSuchFileException e) {
			// TODO: handle exception
			return ResponseEntity.badRequest().body("{ \"message\": \"File Storage Not Found, No Such File\"}");
			// e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return ResponseEntity.badRequest().body("{ \"message\": \"Error Occured when Reading Files\"}");
		}
	}

}
