package com.green.accounts.service;

import com.green.accounts.dto.CustomerDto;

public interface IAccountsService {
    /**
     * Method for creating account
     * @param customerDto - CustomerDto object
     */
    void createAccount(CustomerDto customerDto);

    /**
     * @param mobileNumber
     * @return customerDto
     */
    CustomerDto fetchAccount(String mobileNumber);

    /**
     *
     * @param customerDto
     * @return true or false
     */
    boolean updateAccount(CustomerDto customerDto);

    /**
     *
     * @param mobileNumber
     * @return
     */
    boolean deleteAccount(String mobileNumber);

}
