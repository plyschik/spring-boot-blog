import React, { createContext, useContext, useState } from 'react';
import PropTypes from 'prop-types';
import axios from 'axios';
import { PaginationContext } from './PaginationContext';

const CommentsContext = createContext();

const CommentsProvider = ({ children, postId }) => {
  const {
    currentPage,
    totalPages,
    setCurrentPage,
    setTotalPages,
    setIsFirstPageAvailable,
    setIsPreviousPageAvailable,
    setIsNextPageAvailable,
    setIsLastPageAvailable,
  } = useContext(PaginationContext);

  const [loading, setLoading] = useState(false);
  const [comments, setComments] = useState([]);

  const fetchComments = (page) => {
    setLoading(true);

    axios.get(`/api/posts/${postId}/comments?page=${page}`)
      .then(({ data: { comments: items, pagination } }) => {
        setComments(items);
        setCurrentPage(pagination.currentPage);
        setTotalPages(pagination.totalPages);
        setIsFirstPageAvailable(pagination.currentPage > 0);
        setIsPreviousPageAvailable(pagination.hasPreviousPage);
        setIsNextPageAvailable(pagination.hasNextPage);
        setIsLastPageAvailable(pagination.currentPage < (pagination.totalPages - 1));
      })
      .catch(() => {
        setTimeout(() => {
          fetchComments(page);
        }, 1000);
      })
      .finally(() => {
        setLoading(false);
      });
  };

  const fetchFirstPage = () => {
    fetchComments(0);
  };

  const fetchPreviousPage = () => {
    fetchComments(currentPage - 1);
  };

  const fetchNextPage = () => {
    fetchComments(currentPage + 1);
  };

  const fetchLastPage = () => {
    fetchComments(totalPages - 1);
  };

  return (
    <CommentsContext.Provider
      value={{
        loading,
        comments,
        fetchFirstPage,
        fetchPreviousPage,
        fetchNextPage,
        fetchLastPage,
      }}
    >
      {children}
    </CommentsContext.Provider>
  );
};

CommentsProvider.propTypes = {
  children: PropTypes.node.isRequired,
  postId: PropTypes.string.isRequired,
};

export {
  CommentsContext,
  CommentsProvider,
};
