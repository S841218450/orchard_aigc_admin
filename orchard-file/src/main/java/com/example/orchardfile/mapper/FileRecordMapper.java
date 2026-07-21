package com.example.orchardfile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orchardfile.entity.FileRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件记录 Mapper
 */
@Mapper
public interface FileRecordMapper extends BaseMapper<FileRecord> {
}
