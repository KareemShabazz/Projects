package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Scanner;

public class TransferService {

    private final String BASE_URL;
    private final RestTemplate restTemplate = new RestTemplate();
    public AuthenticatedUser user;
    public ConsoleService consoleService = new ConsoleService();
    public UserService userService = new UserService("http://localhost:8080/user");
    public AccountService accountService = new AccountService("http://localhost:8080/account");


    public TransferService(String url) {
        BASE_URL = url;
    }

    public void setUser(AuthenticatedUser user) {
        this.user = user;
    }

    public Transfer[] findAllTransfersForCurrentUser(AuthenticatedUser user) {
        Transfer[] transferHistory = null;
        try {
            transferHistory = restTemplate.exchange(BASE_URL + "/user/" + user.getUser().getId(), HttpMethod.GET, makeAuthEntity(), Transfer[].class).getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            System.out.println("Transfers cannot be displayed.");
        }
        return transferHistory;
    }

    public Transfer findTransferByTransferId(int transferId) {
        Transfer transfer = null;
        try {
            transfer = restTemplate.exchange(BASE_URL + "/" + transferId, HttpMethod.GET, makeAuthEntity(), Transfer.class).getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            System.out.println("Your transfer could not be found.");
        }
        return transfer;
    }

    public String createTransfer(Transfer transfer) {
        String transferSuccessReport = "";
        try {
            transferSuccessReport = restTemplate.exchange(BASE_URL + "", HttpMethod.POST, makeTransferEntity(transfer), String.class).getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            System.out.println("Transfer failed. Try again.");
        } return transferSuccessReport;
    }

    public void printTransactions(Transfer[] transfers){
        for (Transfer transfer : transfers) {
            transfer.transferDetailsPrintOut();
        }
    }

    public Transfer[] getPendingRequests(AuthenticatedUser user) {
        Transfer[] transfers = null;
        try {
            transfers = restTemplate.exchange(BASE_URL + "/pending/" + user.getUser().getId(), HttpMethod.GET, makeAuthEntity(), Transfer[].class).getBody();
        } catch (RestClientResponseException e) {
            e.printStackTrace();
        }
        return transfers;
    }

    public boolean updateTransfer(Transfer transfer) {
        HttpEntity<Transfer> entity = makeTransferEntity(transfer);
        boolean isSuccessful = false;
        try {
            restTemplate.put(BASE_URL + "/update" + transfer.getTransferId(), entity);
            isSuccessful = true;
        } catch (RestClientResponseException | ResourceAccessException e) {
            System.out.println("Your transfer could not be updated.");
        }
        return isSuccessful;
    }

//    public void sendBucks(AuthenticatedUser user) {
//            accountService.setUser(user);
//            Scanner scanner = new Scanner(System.in);
//            Account userAccount = accountService.findAccountByUserId(user.getUser().getId());
//
//            System.out.print("-------------------------------------------\r\n" + "Enter ID of user you are sending to (or enter 0 to cancel): ");
//
//            Account recipientAccount = accountService.findAccountByUserId(Integer.parseInt(scanner.nextLine()));
//            Transfer transfer = new Transfer();
//            transfer.setAccountTo(recipientAccount.getAccountId());
//            transfer.setAccountFrom(userAccount.getAccountId());
//
//            if (transfer.getAccountTo() != 0) {
//                try {
//                    transfer.setAmount(consoleService.promptForBigDecimal("Enter the amount you would like to transfer: "));
//                } catch (NumberFormatException e) {
//                    System.out.println("An error occurred when amount was input.");
//                }
//                //will print out String returned from sendTransfer method
//                String stringOutput = restTemplate.exchange(BASE_URL + "/send", HttpMethod.POST, makeTransferEntity(transfer), String.class).getBody();
//                System.out.println(stringOutput);
//            }
//    }

//    public void sendBucks() {
//        accountService.setUser(user);
//        Transfer transfer = new Transfer();
//        try {
//            Integer userSelection = consoleService.promptForInt("-------------------------------------------\r\n" +
//                    "Enter ID of user you are sending to (0 to cancel): ");
//            Account recipientAccount = accountService.findAccountByUserId(userSelection);
//            transfer.setAccountTo(recipientAccount.getAccountId());
//            transfer.setAccountFrom((accountService.findAccountByUserId(user.getUser().getId())).getAccountId());
//            if (transfer.getAccountTo() != 0) {
//                try {
//                    transfer.setAmount(consoleService.promptForBigDecimal("Enter amount: "));
//                } catch (NumberFormatException e) {
//                    System.out.println("Error when entering amount");
//                }
//                String output = restTemplate.exchange(BASE_URL + "/send", HttpMethod.POST, makeTransferEntity(transfer), String.class).getBody();
//                System.out.println(output);
//            }
//        } catch (Exception e) {
//            System.out.println("Bad input.");
//        }
//    }

////    BELOW
    public void sendBucks(AuthenticatedUser user) {
        accountService.setUser(user);
        userService.setUser(user);
        int toUserId;
        boolean isUserIdValid = false;

        while (true) {
            String userIdPrompt = "Enter the ID of the user you would like to send money to or enter '0' to cancel this transaction: ";
            toUserId = consoleService.promptForInt(userIdPrompt);

            if (toUserId == 0) {
                return;
            } else if (toUserId == user.getUser().getId()){
                System.out.println("You can't send money to yourself!");
                continue;
            }

            User[] allUsers = userService.findAllUsers();
            for (User u : allUsers) {
                if (u.getId() == (toUserId)) {
                    isUserIdValid = true;
                    break;
                }
            }

            if (isUserIdValid) {
                break;
            } else {
                System.out.println("You have entered an invalid user ID.");
            }
        }

        Account fromAccount = new Account();
        int fromAccountId;
        Account toAccount;
        int toAccountId;

        try {
            fromAccount = accountService.findAccountByUserId(user.getUser().getId());
            fromAccountId = fromAccount.getAccountId();

            toAccount = accountService.findAccountByUserId(toUserId);
            toAccountId = toAccount.getAccountId();
        } catch (Exception e) {
            System.out.println("Unable to retrieve accounts for the transfer. Please try again.");
            return;
        }

        String stringAskingUserForAmount = "Enter the amount you would like to transfer: ";
        BigDecimal amount = consoleService.promptForBigDecimal(stringAskingUserForAmount);

        Transfer transfer = new Transfer();
        transfer.setAccountFrom(fromAccountId);
        transfer.setAccountTo(toAccountId);
        transfer.setAmount(amount);
        transfer.setTransferTypeId(2); // send the transfer
        transfer.setTransferStatusId(2); // approve the transfer

        String transferSuccessReport = createTransfer(transfer);

            System.out.println(transferSuccessReport);
            accountService.getBalance(user);
        }

        //ABOVE

    //SECURITY METHODS BELOW - do not alter

    public HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user.getToken());
        return new HttpEntity<>(headers);
    }

    public HttpEntity<Transfer> makeTransferEntity(Transfer transfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(user.getToken());
        return new HttpEntity<>(transfer, headers);
    }

    //__________________________________________________________________________________________________________________


    //unused methods

    //    public boolean createTransfer(Transfer transfer) {
//        boolean wasCreated = false;
//        try {
//            ResponseEntity<Boolean> response = restTemplate.exchange(BASE_URL + "transfers/",
//                    HttpMethod.POST,
//                    makeTransferEntity(transfer),
//                    Boolean.class);
//            wasCreated = response.getBody();
//            return wasCreated;
//        } catch (RestClientResponseException | ResourceAccessException e) {
//            System.out.println("Transfer failed. Try again.");
//        }
//        return false;
//    }

//    public Transfer[] listAllTransfers() {
//        Transfer[] allTransfers = null;
//        try {
//            ResponseEntity<Transfer[]> response = restTemplate.exchange(BASE_URL + "transfers/",
//                    HttpMethod.GET, makeAuthEntity(), Transfer[].class);
//            allTransfers = response.getBody();
//        } catch (RestClientResponseException | ResourceAccessException e) {
//            System.out.println("Failed to retrieve transfers.");
//        }
//        return allTransfers;
//    }

//    public Transfer[] listAllTransfers() {
//        Transfer[] transfers = null;
//        try {
//            ResponseEntity<Transfer[]> response = restTemplate.exchange(BASE_URL + "s/", HttpMethod.GET, makeAuthEntity(), Transfer[].class);
//            transfers = response.getBody();
//        } catch (RestClientResponseException | ResourceAccessException e) {
//            System.out.println("Failed to retrieve transfers.");
//        }
//        return transfers;
//    }
}
