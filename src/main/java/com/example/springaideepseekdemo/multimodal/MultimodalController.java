package com.example.springaideepseekdemo.multimodal;

import java.io.IOException;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/multimodal")
public class MultimodalController {

	private final ChatClient visionClient;

	private final MultimodalProperties properties;

	public MultimodalController(OllamaChatModel ollamaChatModel, MultimodalProperties properties) {
		this.visionClient = ChatClient.create(ollamaChatModel);
		this.properties = properties;
	}

	@PostMapping(value = "/describe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ImageDescriptionResult describe(@RequestPart("file") MultipartFile file) throws IOException {
		ByteArrayResource imageResource = new ByteArrayResource(file.getBytes()) {
			@Override
			public String getFilename() {
				return file.getOriginalFilename();
			}
		};

		MimeType mimeType = file.getContentType() == null
				? MimeTypeUtils.IMAGE_PNG
				: MimeTypeUtils.parseMimeType(file.getContentType());

		String description = visionClient.prompt()
			.user(u -> u.text(properties.imagePrompt()).media(mimeType, imageResource))
			.call()
			.content();

		return new ImageDescriptionResult(description);
	}

}
