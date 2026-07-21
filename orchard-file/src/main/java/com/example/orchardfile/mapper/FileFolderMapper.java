package com.example.orchardfile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orchardfile.entity.FileFolder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件目录 Mapper
 */
@Mapper
public interface FileFolderMapper extends BaseMapper<FileFolder> {
}
