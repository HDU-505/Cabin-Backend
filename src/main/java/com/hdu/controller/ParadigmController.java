package com.hdu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hdu.config.RedisKeyConfig;
import com.hdu.entity.Paradigm;
import com.hdu.entity.SearchForm;
import com.hdu.service.IParadigmService;
import com.hdu.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author DZL
 * @since 2024-05-28
 */
@RestController
@RequestMapping("/paradigm")
@CrossOrigin
public class ParadigmController {


    @Autowired
    private IParadigmService paradigmService;


    /*
    *   获取所有的范式信息（分页+条件查询）
    * */
    @PostMapping("/getAllParadigm/{current}/{limit}")
    public R getAllParadigm(@PathVariable long current,
                            @PathVariable long limit, @RequestBody SearchForm searchForm) {
        Page<Paradigm> page = new Page<>(current, limit);
        paradigmService.getAllParadigm(page,searchForm); // 这行代码应将分页结果填充到page对象中
        long total = page.getTotal();
        List<Paradigm> records = page.getRecords();
        return R.ok().data("total", total).data("rows", records);
    }

    /*
    *   更新范式信息
    * */
    @PostMapping("/updateParadigm")
    public R updateName(@RequestBody Paradigm paradigm) {
        paradigmService.updateById(paradigm);
        return R.ok().message("名称更新成功");
    }

    /*
    *   批量删除
    * */
    @PostMapping("/deleteParadigm")
    public R deleteByIds(@RequestBody List<String> ids) {
        //删除对应ids下的范式
        boolean flag = paradigmService.deleteByIds(ids);
        return flag ? R.ok().message("删除成功") : R.error().message("删除失败,范式占用中");
    }

    /*
    * 上传封面文件
    * */
    @PostMapping("/uploadCoverFile")
    public R uploadCoverFile(@RequestPart("file") MultipartFile coveFile) {
        boolean flag = paradigmService.uploadCoverFile(coveFile, RedisKeyConfig.EXPERIMENT_PARADIGM_COVER_KEY);
        return flag ? R.ok().message("上传成功") : R.error().message("上传失败");
    }

    /*
    *
    *   上传范式文件
    * */
    @PostMapping("/uploadParadigmFile")
    public R uploadParadigmFile(@RequestPart("file") MultipartFile paradigmFile) {
        boolean flag = paradigmService.uploadCoverFile(paradigmFile,RedisKeyConfig.EXPERIMENT_PARADIGM__FILE_KEY);
        return flag ? R.ok().message("上传成功") : R.error().message("上传失败");
    }

    /*
     *   上传范式信息表单
     * */
    @PostMapping("/uploadForm")
    public R uploadParadigmForm(@RequestBody Paradigm paradigm) {
        boolean flag = paradigmService.uploadParadigm(paradigm);
        return flag ? R.ok().message("上传成功") : R.error().message("上传失败");
    }

    /*
    *   根据文件路径获取文件
    * */
    @PostMapping("/getParadigmFile")
    public ResponseEntity<Resource> getParadigmFile(@RequestBody String encodedUrl) {
        try {
            String url = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.name()).trim();
            //去掉末尾等号
            url = url.substring(0,url.length()-1);
            File file = new File(url);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            FileInputStream fis = new FileInputStream(file);
            InputStreamResource resource = new InputStreamResource(fis);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(file.length());
            headers.setContentDispositionFormData("attachment", file.getName());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /*
    *   根据id获取范式信息
    * */
    @GetMapping("/getParadigmById/{id}")
    public R getParadigmById(@PathVariable String id) {
        return R.ok().data("paradigm",paradigmService.getById(id));
    }

    /*
    *   发送范式到touch screen
    * */
    @GetMapping("/sendParadigmToTouchScreen/{id}")
    public R sendParadigmToTouchScreen(@PathVariable String id) {
        boolean flag = paradigmService.sendParadigmToTouchScreen(id);
        return flag ? R.ok().message("发送成功") : R.error().message("发送失败");
    }

}
