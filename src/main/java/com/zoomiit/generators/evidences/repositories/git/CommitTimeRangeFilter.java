package com.zoomiit.generators.evidences.repositories.git;

import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;

import java.util.Date;

/**
 * The Class CommitTimeRangeFilter.
 * Filters commits based on a time range.
 */
public class CommitTimeRangeFilter extends RevFilter {
  private final long since;
  private final long until;

  /**
   * Instantiates a new CommitTimeRangeFilter.
   *
   * @param since the start date
   * @param until the end date
   */
  public CommitTimeRangeFilter(Date since, Date until) {
    this.since = since.getTime() / 1000;
    this.until = until.getTime() / 1000;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean include(RevWalk walker, RevCommit commit) {
    long commitTime = commit.getCommitTime();
    return commitTime >= since && commitTime <= until;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RevFilter clone() {
    return new CommitTimeRangeFilter(new Date(since * 1000), new Date(until * 1000));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean requiresCommitBody() {
    return false;
  }
}
