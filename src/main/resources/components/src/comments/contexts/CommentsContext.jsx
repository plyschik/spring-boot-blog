import React, { createContext, useReducer } from 'react';
import PropTypes from 'prop-types';
import axios from 'axios';

const FETCHING_COMMENTS = 'FETCHING_COMMENTS';
const FETCH_COMMENTS = 'FETCH_COMMENTS';
const UPDATE_COMMENTS = 'UPDATE_COMMENTS';

const CommentsContext = createContext();

const CommentsProvider = ({ children, postId }) => {
  const reducer = (state, action) => {
    switch (action.type) {
      case FETCHING_COMMENTS:
        return {
          ...state,
          loading: true,
        };
      case FETCH_COMMENTS:
        return {
          ...state,
          loading: false,
          comments: action.payload.comments,
          pagination: action.payload.pagination,
        };
      case UPDATE_COMMENTS:
        return {
          ...state,
          comments: state.comments.map((comment) => {
            if (comment.id === action.payload.id) {
              return action.payload;
            }

            return comment;
          }),
        };
      default:
        return state;
    }
  };

  const initialState = {
    loading: false,
    comments: [],
    pagination: {
      currentPage: 0,
      totalPages: 0,
      totalElements: 0,
      isFirstPageAvailable: false,
      isPreviousPageAvailable: false,
      isNextPageAvailable: false,
      isLastPageAvailable: false,
    },
  };

  const [state, dispatch] = useReducer(reducer, initialState);

  const fetchComments = async (page) => {
    dispatch({ type: FETCHING_COMMENTS });

    const {
      data: {
        comments,
        pagination: {
          currentPage,
          totalPages,
          totalElements,
          hasPreviousPage,
          hasNextPage,
        },
      },
    } = await axios.get(`/api/posts/${postId}/comments?page=${page}`);

    dispatch({
      type: FETCH_COMMENTS,
      payload: {
        comments,
        pagination: {
          currentPage,
          totalPages,
          totalElements,
          isFirstPageAvailable: currentPage > 0,
          isPreviousPageAvailable: hasPreviousPage,
          isNextPageAvailable: hasNextPage,
          isLastPageAvailable: currentPage < totalPages - 1,
        },
      },
    });
  };

  const fetchFirstPage = () => {
    fetchComments(0);
  };

  const fetchPreviousPage = () => {
    fetchComments(state.pagination.currentPage - 1);
  };

  const fetchNextPage = () => {
    fetchComments(state.pagination.currentPage + 1);
  };

  const fetchLastPage = () => {
    fetchComments(state.pagination.totalPages - 1);
  };

  const updateComments = (comment) => {
    dispatch({
      type: UPDATE_COMMENTS,
      payload: comment,
    });
  };

  return (
    <CommentsContext.Provider
      value={{
        state,
        fetchFirstPage,
        fetchPreviousPage,
        fetchNextPage,
        fetchLastPage,
        updateComments,
      }}
    >
      {children}
    </CommentsContext.Provider>
  );
};

CommentsProvider.propTypes = {
  children: PropTypes.node.isRequired,
  postId: PropTypes.number.isRequired,
};

export { CommentsContext, CommentsProvider };
