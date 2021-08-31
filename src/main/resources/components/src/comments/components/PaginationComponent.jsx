import React, { useContext } from 'react';
import Pagination from 'react-bootstrap/Pagination';
import { InternationalizationContext } from '../contexts/InternationalizationContext';
import { PaginationContext } from '../contexts/PaginationContext';
import { CommentsContext } from '../contexts/CommentsContext';

const PaginationComponent = () => {
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
    <Pagination className="justify-content-center">
      <Pagination.Item
        disabled={!isFirstPageAvailable}
        onClick={fetchFirstPage}
      >
        «
      </Pagination.Item>
      <Pagination.Item
        disabled={!isPreviousPageAvailable}
        onClick={fetchPreviousPage}
      >
        ‹
      </Pagination.Item>
      <li className="page-item border border-start-0 border-end-0 px-3 d-flex align-items-center">
        {`${i18n.page} ${currentPage + 1} ${i18n.of} ${totalPages}`}
      </li>
      <Pagination.Item disabled={!isNextPageAvailable} onClick={fetchNextPage}>
        ›
      </Pagination.Item>
      <Pagination.Item disabled={!isLastPageAvailable} onClick={fetchLastPage}>
        »
      </Pagination.Item>
    </Pagination>
  );
};

export default PaginationComponent;
