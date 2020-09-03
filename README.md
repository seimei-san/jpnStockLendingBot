# Japan Stock Lending Bot
This chat bot helps Borrowers and Lenders to reduce human hands from Stock Lending Workflow.
The bot stays on both sides: Borrower and Lender.
Borrower Bot helps the borrower to create RFQ and blast the RFQ to the multiple Lenders (max. 5).
Lender receives the RFQ data in External Chat Room via Elements form and insert the data into his/her own database in order to co-work with downstream systems.
Lender will genrate Quotation based on RFQ data and then return the quotation to Borrower throughout External Chat Room.
Borrower and Lender can engotiate rates in External Chat Room.
Once both parties reach the agreement, Lender can send final quotation or Borrower accept the received quotation.
The quotation data will be stored into the database at borrower side.

## Concept Diagram
![JapanStockLendingBot](https://user-images.githubusercontent.com/53326909/92078914-0c2b6980-edfa-11ea-9a8c-5a00849d966e.jpg)

## Demostration of Bot 
1. Create RFQ from the pasted data
2. Select Lenders for RFQ submission.
3. Send RFQ to the EXT chat rooms based on the selected lenders.
4. In the EXT chat room, Lender accept or reject the received RFQ.
![JapanStockLendingBot_0-1](https://user-images.githubusercontent.com/53326909/92079263-91168300-edfa-11ea-9c17-a4775b33f052.gif)

## Basic Information
* This chat bot is developed based on "Expense Chatbot" in [Symphony Developers Guide/Get Started with Java](https://developers.symphony.com/symphony-developer/docs/get-started-with-java) using Symphony SDK for Java.
* This chat bot can be configured for Borrower or Lender by updating the config file /src/resources/cfg-general.json
* Counter Parties as lenders or borrowers are maintained by /src/resources/csv/counterParties.csv
* Transaction data and counter parties information are stored in the sqlite3 database located in /splite3/stockLending.splite3
* The code does not include the RSA private key should be located in the folder /rsa
* This application is still under development. 
