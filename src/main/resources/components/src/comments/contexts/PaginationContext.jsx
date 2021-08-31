import React, { createContext, useState } from 'react';
import PropTypes from 'prop-types';

const PaginationContext = createContext();

const PaginationProvider = ({ children }) => {
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [isFirstPageAvailable, setIsFirstPageAvailable] = useState(false);
  const [isPreviousPageAvailable, setIsPreviousPageAvailable] = useState(false);
  const [isNextPageAvailable, setIsNextPageAvailable] = useState(false);
  const [isLastPageAvailable, setIsLastPageAvailable] = useState(false);

  return (
    <PaginationContext.Provider
      value={{
        currentPage,
        totalPages,
        totalElements,
        isFirstPageAvailable,
        isPreviousPageAvailable,
        isNextPageAvailable,
        isLastPageAvailable,
        setCurrentPage,
        setTotalPages,
        setTotalElements,
        setIsFirstPageAvailable,
        setIsPreviousPageAvailable,
        setIsNextPageAvailable,
        setIsLastPageAvailable,
      }}
    >
      {children}
    </PaginationContext.Provider>
  );
};

PaginationProvider.propTypes = {
  children: PropTypes.node.isRequired,
};

export {
  PaginationContext,
  PaginationProvider,
};
