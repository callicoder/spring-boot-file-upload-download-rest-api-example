package com.example.filedemo.service;

import com.example.filedemo.exception.FileStorageException;
import com.example.filedemo.exception.MyFileNotFoundException;
import com.example.filedemo.property.FileStorageProperties;

import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

	private final Path fileStorageLocation;
	private final int SHORT_LENGTH = 8;

	@Autowired
	public FileStorageService(FileStorageProperties fileStorageProperties) {
		this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();

		try {
			Files.createDirectories(this.fileStorageLocation);
		} catch (Exception ex) {
			throw new FileStorageException("Could not create the directory where the uploaded files will be stored.",
					ex);
		}
	}

	public String storeFile(MultipartFile file) {
		// Normalize file name
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		String newFileName = null;
		try {
			// Check if the file's name contains invalid characters
			if (fileName.contains("..")) {
				throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
			}

			// generate new file name
			newFileName = generateFileName(fileName);

			// Copy file to the target location (Replacing existing file with the same name)
			Path targetLocation = this.fileStorageLocation.resolve(newFileName);
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

			return newFileName;
		} catch (IOException ex) {
			throw new FileStorageException("Could not store file " + newFileName + ". Please try again!", ex);
		}
	}

	public Resource loadFileAsResource(String fileName) {
		try {
			Path filePath = this.fileStorageLocation.resolve(fileName).normalize();

			// if filepath is null return default image
			if (filePath == null)
				return new ClassPathResource("/static/assets/default_image.jpg");

			Resource resource = new UrlResource(filePath.toUri());
			if (resource.exists()) {
				return resource;
			} else {
				System.out.println("File not found " + fileName);
				// If there is an error, return default image to preview.
				return new ClassPathResource("/static/assets/default_image.jpg");
			}
		} catch (MalformedURLException ex) {
			throw new MyFileNotFoundException("File not found " + fileName, ex);
		}
	}

	public String generateFileName(String fileName) {

		// generate random alphabet
		String shortRandomAlphabet = RandomStringUtils.randomAlphabetic(SHORT_LENGTH).toUpperCase();

		// create date format as string
		String dateStrFormat = DateTime.now().toString("HHmmss_ddMMyyyy");

		// find extension of file
		int indexOfExtension = fileName.indexOf(".");
		String extensionName = fileName.substring(indexOfExtension);

		// return new file name
		return dateStrFormat + "_" + shortRandomAlphabet + extensionName;

	}

	public Path getFileStorageLocation() {
		return this.fileStorageLocation;
	}
}