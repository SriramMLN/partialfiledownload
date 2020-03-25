package com.infosys.filedownload;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author Sriram_L
 *
 */

@RestController
@RequestMapping("/filedownload")
public class ByteServing {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final int DEFAULT_BUFFER_SIZE = 1024;
	private static final String fileBasePath = "c:/temp";

	/*
	 * This is the first step in the chunked file download process. Get the total size of the file and use this
	 * to split either based on number of chunks or the size of each chunk.
	 */
	@GetMapping("/size/{fileName:.+}")
	public ResponseEntity<String> resourceSize(@PathVariable String fileName, HttpServletRequest request)
			throws Exception {
		Path filepath = Paths.get(fileBasePath, fileName);
		Long fileSize = Files.size(filepath);
		return new ResponseEntity<String>(fileSize.toString(), HttpStatus.OK);
	}

	/*
	 * This method does the chunking of the file depending on the chunk size mentioned as part of the request header.
	 * The Range header is used to mention the start position and end position of the file to download. The format to be 
	 * followed in the range request is "Range: bytes=1-15". Validations on the Range header like the requested range 
	 * shouldn't exceed the total size of the file, start range or end range cannot be greater than the total size nor 
	 * end range.
	 */
	@GetMapping("/partialdownload/{fileName:.+}")
	public ResponseEntity downloadResource(@PathVariable String fileName, HttpServletRequest request) throws Exception {

		Path filepath = Paths.get(fileBasePath, fileName);
		int startRange = 0;
		int endRange = 0;
		int sizeOfRange = 0;
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		if (!Files.exists(filepath)) {
			logger.error("File doesn't exist at URI : {}", filepath.toAbsolutePath().toString());
			return ((BodyBuilder) ResponseEntity.notFound()).body("Requested resource is not found.");
		}

		Long length = Files.size(filepath);
		String rangeHeader = request.getHeader("Range");
		if (rangeHeader != null) {
			if (!rangeHeader.matches("^bytes=\\d*-\\d*")) {
				return new ResponseEntity<Object>(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
			}

			for (String part : rangeHeader.substring(6).split(",")) {
				startRange = parseRange(part, 0, part.indexOf("-"));
				endRange = parseRange(part, part.indexOf("-") + 1, part.length());

				if (startRange > endRange || startRange > Math.toIntExact(length)
						|| endRange > Math.toIntExact(length)) {
					return new ResponseEntity<Object>(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
				}
				sizeOfRange = (endRange - startRange) + 1;
				if (startRange == -1) {
					startRange = Math.toIntExact(length) - endRange;
					endRange = Math.toIntExact(length) - 1;
				} else if (endRange == -1 || endRange > Math.toIntExact(length) - 1) {
					endRange = Math.toIntExact(length) - 1;
				}

			}
		}

		try (InputStream input = new BufferedInputStream(Files.newInputStream(filepath))) {
			buffer = new byte[(int) sizeOfRange];
			int read;

			if (length == sizeOfRange) {
				read = input.read(buffer);
			} else {
				input.skip(startRange);
				long remaingRead = sizeOfRange;

				while ((read = input.read(buffer)) > 0) {
					if ((remaingRead -= read) > 0) {
					} else {
						break;
					}
				}
			}
		}
		return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/octet;charset=utf-8"))
				.header(HttpHeaders.CONTENT_RANGE, "bytes "+ startRange + "-" + endRange)
				.header(HttpHeaders.CONTENT_LENGTH, ""+sizeOfRange)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"").body(buffer);
	}

	public int parseRange(String value, int beginIndex, int endIndex) {
		String substring = value.substring(beginIndex, endIndex);
		return (substring.length() > 0) ? Integer.parseInt(substring) : -1;
	}

}