import React, { useContext } from 'react';
import classNames from 'classnames';
import { InternationalizationContext } from '../contexts/InternationalizationContext';
import { PaginationContext } from '../contexts/PaginationContext';
import { CommentsContext } from '../contexts/CommentsContext';

const Pagination = () => {
  const i18n = useContext(InternationalizationContext);

  const {
    isFirstPageAvailable,
    isPreviousPageAvailable,
    isNextPageAvailable,
    isLastPageAvailable,
    currentPage,
    totalPages,
  } = useContext(PaginationContext);

  const {
    loading,
    fetchFirstPage,
    fetchPreviousPage,
    fetchNextPage,
    fetchLastPage,
  } = useContext(CommentsContext);

  if (loading || totalPages <= 1) {
    return null;
  }

  return (
    <nav>
      <ul className="pagination justify-content-center">
        <li className={classNames('page-item', { disabled: !isFirstPageAvailable })}>
          <span className="page-link cursor-pointer" onClick={fetchFirstPage}>&laquo;</span>
        </li>
        <li className={classNames('page-item', { disabled: !isPreviousPageAvailable })}>
          <span className="page-link cursor-pointer" onClick={fetchPreviousPage}>&lsaquo;</span>
        </li>
        <li className="page-item border border-start-0 border-end-0 px-3 d-flex align-items-center">
          {`${i18n.page} ${currentPage + 1} ${i18n.of} ${totalPages}`}
        </li>
        <li className={classNames('page-item', { disabled: !isNextPageAvailable })}>
          <span className="page-link cursor-pointer" onClick={fetchNextPage}>&rsaquo;</span>
        </li>
        <li className={classNames('page-item', { disabled: !isLastPageAvailable })}>
          <span className="page-link cursor-pointer" onClick={fetchLastPage}>&raquo;</span>
        </li>
      </ul>
    </nav>
  );
};

export default Pagination;
