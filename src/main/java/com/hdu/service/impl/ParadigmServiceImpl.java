package com.hdu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hdu.client.ParadigmClient;
import com.hdu.client.ParadigmClientInfo;
import com.hdu.config.RedisKeyConfig;
import com.hdu.entity.Paradigm;
import com.hdu.entity.ParadigmTouchScreen;
import com.hdu.entity.SearchForm;
import com.hdu.mapper.ParadigmMapper;
import com.hdu.service.IParadigmService;
import com.hdu.utils.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author DZL
 * @since 2024-05-28
 */
@Service
public class ParadigmServiceImpl extends ServiceImpl<ParadigmMapper, Paradigm> implements IParadigmService {
    private static final Logger logger = LoggerFactory.getLogger(ParadigmServiceImpl.class);

    @Resource
    private  ParadigmMapper paradigmMapper;

    @Resource
    RedisTemplate<String,Object> redisTemplate;

    @Resource
    ParadigmClientInfo paradigmClientInfo;

    @Resource
    ParadigmClient paradigmClient;

    /*
     *   获取所有的范式信息
     *   分页查询+条件查询
     * */
    @Override
    public void getAllParadigm(Page<Paradigm> page, SearchForm searchForm) {
        // 构建条件
        QueryWrapper<Paradigm> queryWrapper = new QueryWrapper<>();
        if (searchForm != null){
            if (searchForm.getId() != null && !searchForm.getId().isEmpty()){
                queryWrapper.like("id",searchForm.getId());
            }
            if (searchForm.getName() != null && !searchForm.getName().isEmpty()){
                queryWrapper.like("name",searchForm.getName());
            }
        }
        paradigmMapper.selectPage(page,queryWrapper);
    }

    /*
    *   上传范式
    *
    * */

    @Override
    public boolean uploadParadigm(Paradigm paradigm) {
        if (paradigm == null || paradigm.getName().trim().isEmpty()){
            return false;
        }
        // 获取文件路径
        String coverPath = (String) redisTemplate.opsForValue().get(RedisKeyConfig.EXPERIMENT_PARADIGM_COVER_KEY);
        String filePath = (String) redisTemplate.opsForValue().get(RedisKeyConfig.EXPERIMENT_PARADIGM__FILE_KEY);

        // 判断是否文件路径是否为空
        if (coverPath == null || filePath == null){
            return false;
        }

        // 设置范式的封面路径和文件路径
        paradigm.setCoverPath(coverPath);
        paradigm.setFilePath(filePath);

        // 保存范式信息
        try {
            int count = paradigmMapper.insert(paradigm);
            if (count < 1){
                return false;
            }else{
                // 上传成功后清除redis中的缓存
                redisTemplate.delete(RedisKeyConfig.EXPERIMENT_PARADIGM_COVER_KEY);
                redisTemplate.delete(RedisKeyConfig.EXPERIMENT_PARADIGM__FILE_KEY);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            logger.error("上传范式失败...");
            return false;
        }
    }


    /*
    *   上传文件
    * */
    @Override
    public boolean uploadCoverFile(MultipartFile file,String type) {
        if (type.equals(RedisKeyConfig.EXPERIMENT_PARADIGM_COVER_KEY)){
            // 判断文件是否为图片
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image")){
                return false;
            }
            // 判断是否已经上传过，如果上传过则覆盖掉原来的
            String coverPath = (String) redisTemplate.opsForValue().get(RedisKeyConfig.EXPERIMENT_PARADIGM_COVER_KEY);
            if (coverPath != null){
                deleteFile(coverPath);
            }
        }else {
            // 判断文件是否为zip压缩包
            String contentType = file.getContentType();
            if (contentType == null || !contentType.contains("zip")){
                return false;
            }
            // 判断是否已经上传过，如果上传过则覆盖掉原来的
            String filePath = (String) redisTemplate.opsForValue().get(RedisKeyConfig.EXPERIMENT_PARADIGM__FILE_KEY);
            if (filePath != null){
                deleteFile(filePath);
            }
        }

        // 将文件保存到本地缓存文件夹中，并记录下路径
        String projectPath = Paths.get("").toAbsolutePath().toString();
        String uploadDir = projectPath + "/uploads/";
        new File(uploadDir).mkdirs();
        try {
            String fileOriginalName = file.getOriginalFilename();
            String fileExtension = fileOriginalName.substring(fileOriginalName.lastIndexOf("."));
            String fileUUIDName = IdGenerator.generate20CharId() + fileExtension;
            String filePath = uploadDir + fileUUIDName;
            file.transferTo(new File(filePath));
            // 将文件路径缓存在redis
            if (type.equals(RedisKeyConfig.EXPERIMENT_PARADIGM_COVER_KEY)) {
                redisTemplate.opsForValue().set(RedisKeyConfig.EXPERIMENT_PARADIGM_COVER_KEY, filePath);
            }else {
                redisTemplate.opsForValue().set(RedisKeyConfig.EXPERIMENT_PARADIGM__FILE_KEY, filePath);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("封面文件上传失败...");
        }
        return false;
    }


    /*
    *   根据文件路径获取文件
    * */
    @Override
    public Object getParadigmFile(String url) {
        try {
            File file = new File(url);
            return file;
        }catch (Exception e){
            e.printStackTrace();
            logger.error("获取文件失败...");
        }
        return null;
    }

    /*
    *   发送范式数据到touch screen
    * */
    @Override
    public boolean sendParadigmToTouchScreen(String id) {
        // 获取范式信息
        Paradigm paradigm = paradigmMapper.selectById(id);

        // 将 Paradigm 转换成 ParadigmTouchScreen
        ParadigmTouchScreen paradigmTouchScreen = new ParadigmTouchScreen();
        paradigmTouchScreen.setId(paradigm.getId());
        paradigmTouchScreen.setName(paradigm.getName());
        paradigmTouchScreen.setCoverPath(paradigm.getCoverPath());
        paradigmTouchScreen.setFilePath(paradigm.getFilePath());
        paradigmTouchScreen.setCreateTime(paradigm.getCreateTime().toString());
        paradigmTouchScreen.setUpdateTime(paradigm.getUpdateTime().toString());

        // 获取文件地址
        String filePath = paradigm.getFilePath();
        String coverPath = paradigm.getCoverPath();

        // 将文件转换成 MultipartFile
        MultipartFile multipartFile = null;
        MultipartFile coverFile = null;
        try {
            multipartFile = convert(new File(filePath));
            coverFile = convert(new File(coverPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        boolean flag2 = paradigmClient.forwardParadigm(coverFile, multipartFile);
        if (!flag2){
            return false;
        }
        //把信息传递给touch screen
        boolean flag = paradigmClientInfo.forwardParadigmInfo(paradigmTouchScreen);
        // 调用 Feign 客户端
        return flag;
    }


    public static MultipartFile convert(File file) throws IOException {
        // 读取文件内容
        byte[] content = new byte[(int) file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(content);
        fileInputStream.close();

        // 创建一个 MockMultipartFile 对象
        MultipartFile multipartFile = new MockMultipartFile(
                file.getName(),   // 文件名
                file.getName(),   // 原始文件名
                Files.probeContentType(file.toPath()),  // 文件类型
                content          // 文件内容
        );

        return multipartFile;
    }
    /*
    *   批量删除
    * */
    @Override
    public boolean deleteByIds(List<String> ids) {
        // 获取所有的范式信息
        List<Paradigm> list = paradigmMapper.selectBatchIds(ids);

        //获取文件路径
        for (Paradigm paradigm : list){
            try{

                String coverPath = paradigm.getCoverPath();
                String filePath = paradigm.getFilePath();

                //删除文件
                deleteFile(coverPath);
                deleteFile(filePath);

                paradigmMapper.deleteById(paradigm.getId());
            }catch (DataIntegrityViolationException e){
                e.printStackTrace();
                logger.error("范式："+paradigm.getId()+"已被某实验使用中，无法删除!");
                return false;
            } catch (Exception e){
                e.printStackTrace();
                logger.error("批量删除范式失败...");
                return false;
            }
        }
        //返回结果
        return true;
    }



    // 删除文件的方法
    private void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

}
