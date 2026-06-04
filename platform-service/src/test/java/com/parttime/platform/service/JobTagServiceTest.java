package com.parttime.platform.service;

import com.parttime.platform.mapper.JobTagMapper;
import com.parttime.platform.pojo.cmd.JobTagCmd;
import com.parttime.platform.pojo.cmd.JobTagGroupCmd;
import com.parttime.platform.pojo.entity.JobTag;
import com.parttime.platform.pojo.entity.JobTagGroup;
import com.parttime.platform.pojo.vo.JobTagGroupVO;
import com.parttime.platform.pojo.vo.JobTagVO;
import com.parttime.platform.service.impl.JobTagServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobTagServiceTest {

    @Mock
    private JobTagMapper jobTagMapper;

    @InjectMocks
    private JobTagServiceImpl jobTagService;

    @Test
    void getAllGroups_shouldReturnGroupsWithNestedTagsSortedBySortOrderThenId() {
        JobTagGroup laterGroup = group(2L, "Later", "later", 10);
        JobTagGroup firstGroup = group(1L, "First", "first", 5);
        JobTag laterTag = tag(3L, 1L, "Later Tag", "later_tag", 2);
        JobTag firstTag = tag(2L, 1L, "First Tag", "first_tag", 1);
        JobTag tieBreakTag = tag(1L, 1L, "Tie Break Tag", "tie_break_tag", 1);

        when(jobTagMapper.findAllGroups()).thenReturn(List.of(laterGroup, firstGroup));
        when(jobTagMapper.findAllTags()).thenReturn(List.of(laterTag, firstTag, tieBreakTag));

        List<JobTagGroupVO> result = jobTagService.getAllGroups();

        assertThat(result).extracting(JobTagGroupVO::getId).containsExactly(1L, 2L);
        assertThat(result.get(0).getTags()).extracting(JobTagVO::getId).containsExactly(1L, 2L, 3L);
    }

    @Test
    void createGroup_blankStatus_shouldDefaultActive() {
        JobTagGroupCmd cmd = new JobTagGroupCmd();
        cmd.setName("Group");
        cmd.setCode("group");
        cmd.setSortOrder(1);
        cmd.setStatus(" ");

        doAnswer(invocation -> {
            JobTagGroup group = invocation.getArgument(0);
            group.setId(10L);
            return 1;
        }).when(jobTagMapper).insertGroup(any(JobTagGroup.class));

        JobTagGroupVO response = jobTagService.createGroup(cmd);

        assertThat(response.getStatus()).isEqualTo("ACTIVE");
        verify(jobTagMapper).insertGroup(argThat(group -> "ACTIVE".equals(group.getStatus())));
    }

    @Test
    void createTag_blankStatus_shouldDefaultActive() {
        JobTagCmd cmd = new JobTagCmd();
        cmd.setGroupId(1L);
        cmd.setName("Tag");
        cmd.setCode("tag");
        cmd.setSortOrder(1);
        cmd.setStatus(" ");

        doAnswer(invocation -> {
            JobTag tag = invocation.getArgument(0);
            tag.setId(20L);
            return 1;
        }).when(jobTagMapper).insertTag(any(JobTag.class));

        JobTagVO response = jobTagService.createTag(cmd);

        assertThat(response.getStatus()).isEqualTo("ACTIVE");
        verify(jobTagMapper).insertTag(argThat(tag -> "ACTIVE".equals(tag.getStatus())));
    }

    @Test
    void deleteGroup_shouldSoftDisableGroupAndTags() {
        JobTagGroup existing = group(1L, "Group", "group", 1);
        existing.setStatus("ACTIVE");
        when(jobTagMapper.findGroupById(1L)).thenReturn(Optional.of(existing));

        jobTagService.deleteGroup(1L);

        verify(jobTagMapper).disableGroup(1L);
        verify(jobTagMapper).disableTagsByGroupId(1L);
    }

    @Test
    void deleteGroup_shouldBeTransactional() throws Exception {
        Method method = JobTagServiceImpl.class.getMethod("deleteGroup", Long.class);

        assertThat(method.getAnnotation(Transactional.class)).isNotNull();
    }

    @Test
    void deleteTag_shouldSoftDisableTag() {
        JobTag existing = tag(1L, 2L, "Tag", "tag", 1);
        existing.setStatus("ACTIVE");
        when(jobTagMapper.findTagById(1L)).thenReturn(Optional.of(existing));

        jobTagService.deleteTag(1L);

        verify(jobTagMapper).disableTag(1L);
    }

    private JobTagGroup group(Long id, String name, String code, Integer sortOrder) {
        JobTagGroup group = new JobTagGroup();
        group.setId(id);
        group.setName(name);
        group.setCode(code);
        group.setSortOrder(sortOrder);
        group.setStatus("ACTIVE");
        return group;
    }

    private JobTag tag(Long id, Long groupId, String name, String code, Integer sortOrder) {
        JobTag tag = new JobTag();
        tag.setId(id);
        tag.setGroupId(groupId);
        tag.setName(name);
        tag.setCode(code);
        tag.setSortOrder(sortOrder);
        tag.setStatus("ACTIVE");
        return tag;
    }
}
