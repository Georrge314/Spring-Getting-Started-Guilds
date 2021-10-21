package com.example;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.storage.StorageFileNotFoundException;
import com.example.storage.StorageService;

import java.util.stream.Collectors;


@Controller
public class FileUploadController {
    private final StorageService storageService;

    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }


    @GetMapping("/")
    public String listUploadedFiles(Model model) {
        //key: "files", value: list.of(uris) example: 'http://localhost:8080/filse/...'
        model.addAttribute("files",
                storageService.loadAll()
                        .map(path -> {
                                    return MvcUriComponentsBuilder
                                            .fromMethodName(
                                                    FileUploadController.class,
                                                    "serverFile",
                                                    path.getFileName().toString())
                                            .build().toUri().toString();
                                }
                        ).collect(Collectors.toList()));

        //render model to html
        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serverFile(@PathVariable String filename) {
        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        storageService.store(file);

        redirectAttributes
                .addFlashAttribute(
                        "message",
                        "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }


    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }
}
