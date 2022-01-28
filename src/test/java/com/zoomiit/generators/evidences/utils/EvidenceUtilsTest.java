/**
 * EvidenceUtilsTest.java 25 ene 2022
 *
 * Copyright 2022 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;

import java.util.Arrays;
import java.util.List;

import com.zoomiit.generators.evidences.configuration.AppConfiguration;
import com.zoomiit.generators.evidences.mappers.FileTypeMapperImpl;
import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, RandomBeansExtension.class})
@ContextConfiguration(classes = {EvidenceUtils.class, FileTypeMapperImpl.class})
class EvidenceUtilsTest {

  @Autowired
  private EvidenceUtils evidenceUtils;

  @MockBean
  private AppConfiguration appConfiguration;

  @Test
  void testEvaluateAddCommit_all_empty() {
    // Given

    Mockito.when(this.appConfiguration.getExcludeCommits()).thenReturn(Lists.emptyList());
    Mockito.when(this.appConfiguration.getJiraCodes()).thenReturn(Lists.emptyList());

    // When
    final boolean result =
        this.evidenceUtils.evaluateAddCommit("Merge branch 'master' of https://host/bitbucket/main into");

    // Then

    Mockito.verify(this.appConfiguration, times(1)).getExcludeCommits();
    Mockito.verify(this.appConfiguration, times(1)).getJiraCodes();

    assertThat(result, is(true));
  }

  @Test
  void testEvaluateAddCommit_with_jira_code(@Random(size = 5, type = String.class) final List<String> jiraCodes) {
    // Given

    Mockito.when(this.appConfiguration.getExcludeCommits()).thenReturn(Lists.emptyList());
    Mockito.when(this.appConfiguration.getJiraCodes()).thenReturn(jiraCodes);

    // When
    final boolean result =
        this.evidenceUtils.evaluateAddCommit("Merge branch 'master' of https://host/bitbucket/main into");

    // Then

    Mockito.verify(this.appConfiguration, times(1)).getExcludeCommits();
    Mockito.verify(this.appConfiguration, times(1)).getJiraCodes();

    assertThat(result, is(false));
  }

  @Test
  void testEvaluateAddCommit_with_exclude_words_ok(
      @Random(size = 5, type = String.class) final List<String> excludeWords) {
    // Given

    Mockito.when(this.appConfiguration.getExcludeCommits()).thenReturn(excludeWords);
    Mockito.when(this.appConfiguration.getJiraCodes()).thenReturn(Lists.emptyList());

    // When
    final boolean result =
        this.evidenceUtils.evaluateAddCommit("Merge branch 'master' of https://host/bitbucket/main into");

    // Then

    Mockito.verify(this.appConfiguration, times(1)).getExcludeCommits();
    Mockito.verify(this.appConfiguration, times(1)).getJiraCodes();

    assertThat(result, is(true));
  }

  @Test
  void testEvaluateAddCommit_with_exclude_words_ko(
      @Random(size = 5, type = String.class) final List<String> jiracodes) {
    // Given

    Mockito.when(this.appConfiguration.getExcludeCommits()).thenReturn(Arrays.asList("Merge branch"));
    Mockito.when(this.appConfiguration.getJiraCodes()).thenReturn(Lists.emptyList());

    // When
    final boolean result =
        this.evidenceUtils.evaluateAddCommit("Merge branch 'master' of https://host/bitbucket/main into");

    // Then

    Mockito.verify(this.appConfiguration, times(1)).getExcludeCommits();
    Mockito.verify(this.appConfiguration, times(1)).getJiraCodes();

    assertThat(result, is(false));
  }

}
