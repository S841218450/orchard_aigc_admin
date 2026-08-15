package com.example.orchardai.service;

import com.example.orchardai.dto.KnowledgeDocQueryDto;
import com.example.orchardai.dto.KnowledgeDocStatusUpdateDto;
import com.example.orchardai.dto.KnowledgeDocVo;
import com.example.orchardcommon.result.PageResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface KnowledgeDocService {

    /**
     * 上传知识库文档（二进制 → 上传COS → 写入knowledge_doc，一步到位）
     *
     * @param file     二进制文件（MultipartFile）
     * @param folderId 知识库目录ID（null或0 = 根目录）
     * @param userId   当前用户ID
     */
    KnowledgeDocVo upload(MultipartFile file, Long folderId, Long userId);

    /**
     * 批量上传知识库文档（每个文件独立走"传COS→落库→触发Agent"流程）
     *
     * @param files    文件列表
     * @param folderId 知识库目录ID（null或0 = 根目录，整批统一）
     * @param userId   当前用户ID
     */
    List<KnowledgeDocVo> uploadBatch(List<MultipartFile> files, Long folderId, Long userId);

    void updateStatus(KnowledgeDocStatusUpdateDto dto);

    /**
     * 分页查询知识库文档列表
     *
     * @param query  查询参数（extends BaseQuery：query模糊搜索、pageNum/pageSize）
     * @param userId 当前用户ID
     */
    PageResult<KnowledgeDocVo> page(KnowledgeDocQueryDto query, Long userId);

    void delete(Long id, Long userId);

    /**
     * 重试向量入库（仅 待处理/失败 状态可重试，重置为待处理后重新触发 Agent 向量化）
     *
     * @param id     文档ID
     * @param userId 当前用户ID
     */
    void retry(Long id, Long userId);

    KnowledgeDocVo getById(Long id, Long userId);
}
