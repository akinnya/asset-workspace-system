package com.xxr.lingtuthinktank.opensource;

import com.xxr.lingtuthinktank.model.dto.space.command.SpaceEditRequest;
import com.xxr.lingtuthinktank.model.dto.space.command.SpaceUpdateRequest;
import com.xxr.lingtuthinktank.model.entity.picture.Picture;
import com.xxr.lingtuthinktank.model.entity.space.Space;
import com.xxr.lingtuthinktank.model.vo.picture.PictureVO;
import com.xxr.lingtuthinktank.model.vo.space.SpaceVO;
import com.xxr.lingtuthinktank.model.vo.user.UserProfileSummaryVO;
import com.xxr.lingtuthinktank.model.vo.user.UserStatsVO;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenSourceSanitizationStructureTest {

    @Test
    void legacyCharacterAndMusicPackagesShouldBeRemoved() {
        List<String> paths = Arrays.asList(
                "src/main/java/com/xxr/lingtuthinktank/mapper/character",
                "src/main/java/com/xxr/lingtuthinktank/model/dto/character",
                "src/main/java/com/xxr/lingtuthinktank/model/entity/character",
                "src/main/java/com/xxr/lingtuthinktank/model/vo/character",
                "src/main/java/com/xxr/lingtuthinktank/service/character",
                "src/main/java/com/xxr/lingtuthinktank/model/dto/music"
        );
        List<String> existingPaths = paths.stream()
                .filter(path -> Files.exists(Paths.get(path)))
                .collect(Collectors.toList());
        assertTrue(existingPaths.isEmpty(), "仍存在待删除的非核心目录: " + existingPaths);
    }

    @Test
    void pictureDomainShouldNotExposeLegacyCharacterFields() {
        assertFalse(hasField(Picture.class, "characterId"), "Picture 不应再暴露 characterId");
        assertFalse(hasField(PictureVO.class, "characterId"), "PictureVO 不应再暴露 characterId");
        assertFalse(hasField(PictureVO.class, "characterIdStr"), "PictureVO 不应再暴露 characterIdStr");
        assertFalse(hasField(PictureVO.class, "characterName"), "PictureVO 不应再暴露 characterName");
        assertFalse(hasField(PictureVO.class, "characterCanDownload"), "PictureVO 不应再暴露 characterCanDownload");
    }

    @Test
    void spaceDomainShouldNotExposeLegacyRelationGraphPayload() throws IOException {
        assertFalse(hasField(Space.class, "socialData"), "Space 不应再暴露 socialData");
        assertFalse(hasField(SpaceVO.class, "socialData"), "SpaceVO 不应再暴露 socialData");
        assertFalse(hasField(SpaceEditRequest.class, "socialData"), "SpaceEditRequest 不应再暴露 socialData");
        assertFalse(hasField(SpaceUpdateRequest.class, "socialData"), "SpaceUpdateRequest 不应再暴露 socialData");
        String sql = Files.readString(Paths.get("src/main/resources/sql/create_table.sql"));
        assertFalse(sql.contains("socialData"), "初始化 SQL 不应再暴露 socialData");
    }

    @Test
    void userSummaryShouldNotExposeLegacyCharacterMetrics() {
        assertFalse(hasField(UserStatsVO.class, "characterCount"), "UserStatsVO 不应再暴露 characterCount");
        assertFalse(hasField(UserProfileSummaryVO.class, "myOcs"), "UserProfileSummaryVO 不应再暴露 myOcs");
    }

    private boolean hasField(Class<?> type, String fieldName) {
        for (Field field : type.getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }
}
