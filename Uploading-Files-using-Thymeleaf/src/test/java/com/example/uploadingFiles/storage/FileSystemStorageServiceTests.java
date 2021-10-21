package com.example.uploadingFiles.storage;

import com.example.storage.FIleSystemStorageService;
import com.example.storage.StorageException;
import com.example.storage.StorageProperties;
import com.example.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileSystemStorageServiceTests {

    private StorageProperties properties = new StorageProperties();
    private StorageService storageService;

    @BeforeEach
    public void init() {
        properties.setLocation("target/files/" + Math.abs(new Random().nextLong()));
        storageService = new FIleSystemStorageService(properties);
        storageService.init();
    }

    @Test
    public void loadNonExistent() {
        assertThat(
                storageService.load("foo.txt")).doesNotExist();
    }

    @Test
    public void saveAndLoad() {
        storageService.store(
                new MockMultipartFile(
                        "foo",
                        "foo.txt",
                        MediaType.TEXT_PLAIN_VALUE,
                        "Hello, World".getBytes()));

        assertThat(storageService.load("foo.txt")).exists();
    }

    @Test
    public void saveRelativePathNotPermitted() {
        assertThrows(StorageException.class, () -> {
            storageService.store(
                    new MockMultipartFile(
                            "foo",
                            //relative path(../) not permitted
                            "../foo.txt",
                            MediaType.TEXT_PLAIN_VALUE,
                            "Hello, World".getBytes()));
        });
    }

    @Test
    public void saveAbsolutePathNotPermitted() {
        assertThrows(StorageException.class, () -> {
            storageService.store(
                    new MockMultipartFile(
                            "foo",
                            //save abs path(/something/something)
                            "/etc/passwd",
                            MediaType.TEXT_PLAIN_VALUE,
                            "Hello, World".getBytes()));
        });
    }

    @Test
    @EnabledOnOs({OS.MAC})
    public void saveAbsolutePathInFilenamePermitted() {
        //Unix file systems (e.g. ext4) allows backslash '\' in file names.
        String fileName = "\\etc\\passwd";
        storageService.store(
                new MockMultipartFile(
                        fileName,
                        fileName,
                        MediaType.TEXT_PLAIN_VALUE,
                        "Hello, World".getBytes()));
        assertTrue(Files.exists(
                Paths.get(
                        properties.getLocation())
                        .resolve(Paths.get(fileName))));
    }

    @Test
    public void savePermitted() {
        storageService.store(
                new MockMultipartFile(
                        "foo",
                        "bar/../foo.txt",
                        MediaType.TEXT_PLAIN_VALUE,
                        "Hello, World".getBytes()));
    }

}
