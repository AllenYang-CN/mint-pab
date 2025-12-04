package com.allen.controller;

import com.allen.common.ResponseResult;
import com.allen.common.ResponseUtils;
import com.allen.entity.Tag;
import com.allen.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签Controller
 */
@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    /**
     * 创建标签
     */
    @PostMapping
    public ResponseEntity<ResponseResult<Tag>> createTag(@RequestBody Tag tag) {
        int result = tagService.createTag(tag);
        return ResponseUtils.ok("标签创建成功", tag);
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseResult<Void>> deleteTag(@PathVariable Integer id) {
        int result = tagService.deleteTag(id);
        return ResponseUtils.ok("标签删除成功");
    }

    /**
     * 更新标签
     */
    @PutMapping
    public ResponseEntity<ResponseResult<Tag>> updateTag(@RequestBody Tag tag) {
        int result = tagService.updateTag(tag);
        return ResponseUtils.ok("标签更新成功", tag);
    }

    /**
     * 根据ID获取标签
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseResult<Tag>> getTagById(@PathVariable Integer id) {
        Tag tag = tagService.getTagById(id);
        return ResponseUtils.ok("标签查询成功", tag);
    }

    /**
     * 获取所有标签
     */
    @GetMapping
    public ResponseEntity<ResponseResult<List<Tag>>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        return ResponseUtils.ok("标签列表查询成功", tags);
    }
}
