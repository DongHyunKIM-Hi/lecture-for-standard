package org.example.lectureforstandard.post.controller;

import lombok.RequiredArgsConstructor;
import org.example.lectureforstandard.post.model.dto.CreatePostRequest;
import org.example.lectureforstandard.post.model.dto.PostResponse;
import org.example.lectureforstandard.post.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse createPost(@RequestBody CreatePostRequest request) {
        return PostResponse.from(postService.createPost(request.title()));
    }

    @GetMapping("/{postId}")
    public PostResponse getPost(@PathVariable Long postId) {
        return PostResponse.from(postService.getPost(postId));
    }
}
