package com.receipthub.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.receipthub.dto.ReceiptUploadResponse;
import com.receipthub.dto.ReimbursementSubmitRequest;
import com.receipthub.model.User;

@Service
public class TelegramBotService extends TelegramLongPollingBot {
    
    private static final Logger log = LoggerFactory.getLogger(TelegramBotService.class);
    
    @Value("${telegram.bot.username}")
    private String botUsername;
    
    private final UserService userService;
    private final ReceiptService receiptService;
    private final ReimbursementService reimbursementService;
    
    public TelegramBotService(
            @Value("${telegram.bot.token}") String botToken,
            UserService userService,
            ReceiptService receiptService,
            ReimbursementService reimbursementService) {
        super(botToken);
        this.userService = userService;
        this.receiptService = receiptService;
        this.reimbursementService = reimbursementService;
    }
    
    // Store pending receipts waiting for description
    // Map: chatId -> PendingReceipt
    private final Map<Long, PendingReceipt> pendingReceipts = new HashMap<>();
    
    // Inner class to track pending receipt data
    private static class PendingReceipt {
        Long receiptId;
        Double amount;
        String merchantName;
        
        PendingReceipt(Long receiptId, Double amount, String merchantName) {
            this.receiptId = receiptId;
            this.amount = amount;
            this.merchantName = merchantName;
        }
    }
    
    @Override
    public String getBotUsername() {
        return botUsername;
    }
    
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();
            
            // Check if user is already registered with this chat ID
            Optional<User> userOpt = userService.getUserByTelegramChatId(chatId);
            
            if (userOpt.isEmpty()) {
                // User not linked yet - handle registration
                handleUnregisteredUser(update, chatId);
                return;
            }
            
            User user = userOpt.get();
            
            // Validate file uploads for registered users
            if (update.getMessage().hasPhoto()) {
                // Check for media group (multiple photos uploaded together)
                if (update.getMessage().getMediaGroupId() != null) {
                    sendMessage(chatId, 
                        """
                        ❌ Multiple photos detected!
                        
                        ⚠️ Please send ONLY ONE receipt photo at a time.
                        
                        📸 Send your receipt photos one by one for processing.""");
                    return;
                }
                handlePhotoMessage(update, user);
            } else if (update.getMessage().hasText()) {
                handleTextMessage(update, user);
            } else if (update.getMessage().hasDocument() || update.getMessage().hasVideo() || 
                       update.getMessage().hasAudio() || update.getMessage().hasVoice() || 
                       update.getMessage().hasSticker() || update.getMessage().hasVideoNote()) {
                // Reject non-photo file types
                sendMessage(chatId, 
                    """
                    ❌ Invalid file type.
                    
                    📸 Please send a PHOTO of your receipt only.
                    
                    ⚠️ Documents, videos, and other file types are not supported.""");
            } else {
                // Unknown message type
                sendMessage(chatId, 
                    """
                    ❌ Unsupported message type.
                    
                    📸 Please send a photo of your receipt or use /start for help.""");
            }
        }
    }
    
    private void handleUnregisteredUser(Update update, Long chatId) {
        if (update.getMessage().hasContact()) {
            handleContactShared(update, chatId);
            return;
        }
        
        if (update.getMessage().hasText() && update.getMessage().getText().equals("/start")) {
            String username = update.getMessage().getFrom().getFirstName();
            sendMessageWithContactRequest(chatId, 
                """
                👋 Welcome to ReceiptHub, %s!
                
                To use this bot, we need to verify your phone number.
                
                📱 Please click the button below to share your phone number."""
                .formatted(username));
        } else {
            sendMessageWithContactRequest(chatId, 
                "Please share your phone number to register.");
        }
    }
    
    private void handleContactShared(Update update, Long chatId) {
        Contact contact = update.getMessage().getContact();
        String phoneNumber = normalizePhoneNumber(contact.getPhoneNumber());
        
        // Verify this is the user's own phone number
        if (!contact.getUserId().equals(update.getMessage().getFrom().getId())) {
            sendMessage(chatId, 
                """
                ❌ Please share your own contact, not someone else's.
                
                Click the button below to share your phone number.""");
            return;
        }
        
        // Look up user by phone number
        Optional<User> userByPhone = userService.getUserByPhoneNumber(phoneNumber);
        
        if (userByPhone.isPresent()) {
            User user = userByPhone.get();
            
            // Check if this user already has a different chat ID linked
            if (user.getTelegramChatId() != null && !user.getTelegramChatId().equals(chatId)) {
                sendMessage(chatId, 
                    """
                    ❌ This phone number is already linked to another Telegram account.
                    
                    Please contact your administrator if you need help.""");
                return;
            }
            
            // Link this chat ID to the user
            userService.linkTelegramChatId(user.getId(), chatId);
            
            sendMessageWithKeyboardRemove(chatId, 
                """
                ✅ Registration successful!
                
                👤 Welcome, %s!
                📧 Email: %s
                📱 Phone: %s
                
                📸 You can now send receipt photos for reimbursement requests.
                
                Use /start anytime for help."""
                .formatted(user.getName(), user.getEmail(), user.getPhoneNumber()));
        } else {
            sendMessage(chatId, 
                """
                ❌ Phone number %s not found in our system.
                
                Please make sure you're registered with the company.
                Contact your administrator for assistance."""
                .formatted(phoneNumber));
        }
    }
    
    private void sendMessageWithContactRequest(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        
        // Create keyboard with contact sharing button
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton("📱 Share Phone Number");
        button.setRequestContact(true);
        row.add(button);
        
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        
        message.setReplyMarkup(keyboardMarkup);
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send Telegram message with contact request: {}", e.getMessage(), e);
        }
    }
    
    private void sendMessageWithKeyboardRemove(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(new ReplyKeyboardRemove(true));
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send Telegram message with keyboard remove: {}", e.getMessage(), e);
        }
    }
    
    private String normalizePhoneNumber(String input) {
        if (input.startsWith("+")) {
            return input.replaceAll("[^0-9+]", "");
        }
        return "+" + input.replaceAll("[^0-9]", "");
    }
    
    private void handlePhotoMessage(Update update, User user) {
        File downloadedFile = null;
        try {
            PhotoSize photo = update.getMessage().getPhoto().stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .orElse(null);
            
            if (photo != null) {
                // Download the photo
                org.telegram.telegrambots.meta.api.objects.File file = 
                    execute(new org.telegram.telegrambots.meta.api.methods.GetFile(photo.getFileId()));
                
                downloadedFile = downloadFile(file);
                byte[] fileData = Files.readAllBytes(downloadedFile.toPath());
                
                // Upload receipt and extract OCR data (merchant + amount only)
                ReceiptUploadResponse response = receiptService.uploadReceiptFromBytes(
                    fileData, 
                    "receipt_" + System.currentTimeMillis() + ".jpg"
                );
                
                // Check if OCR was successful
                if (response.getMerchantName() == null || response.getMerchantName().equals("OCR Extraction Failed") 
                    || response.getAmount() == null || response.getAmount() == 0.0) {
                    
                    // OCR failed - delete the uploaded file
                    receiptService.deleteReceipt(response.getReceiptId());
                    
                    sendMessage(user.getTelegramChatId(), 
                        """
                        Receipt Processing Failed
                        
                        We couldn't read the receipt properly. This could be because:
                        • The image is too blurry or unclear
                        • The receipt text is not readable
                        • Poor lighting conditions
                        
                        Please try again with:
                        ✓ Better lighting
                        ✓ Clear, focused photo
                        ✓ Receipt laid flat
                        
                        Send another receipt photo when ready!""");
                    return;
                }
                
                // Store the pending receipt data
                PendingReceipt pendingReceipt = new PendingReceipt(
                    response.getReceiptId(),
                    response.getAmount(),
                    response.getMerchantName()
                );
                pendingReceipts.put(user.getTelegramChatId(), pendingReceipt);
                
                // Ask user for description
                sendMessage(user.getTelegramChatId(), 
                    """
                    Receipt uploaded successfully!
                    
                    Receipt Details (from OCR):
                    Merchant: %s
                    Amount: $%s
                    
                    Please type a description for this expense:
                    (e.g., "Team lunch", "Office supplies", "Client meeting dinner")"""
                    .formatted(response.getMerchantName(), String.format("%.2f", response.getAmount())));
            }
        } catch (TelegramApiException | IOException e) {
            sendMessage(user.getTelegramChatId(), 
                """
               Error uploading receipt. Please try again or contact support.
               \s
                Error details: %s"""
                .formatted(e.getMessage()));
        } finally {

            if (downloadedFile != null && downloadedFile.exists()) {
                downloadedFile.delete();
            }
        }
    }
    
    private void handleTextMessage(Update update, User user) {
        String text = update.getMessage().getText();
        Long chatId = user.getTelegramChatId();
        if (pendingReceipts.containsKey(chatId)) {
            if (text.equals("/start") || text.equals("/cancel")) {
                pendingReceipts.remove(chatId);
                sendMessage(chatId, 
                    """
                    Receipt submission cancelled.
                    
                    Send a new receipt photo to start over.""");
                return;
            }
            

            PendingReceipt pending = pendingReceipts.get(chatId);
            
            try {
                ReimbursementSubmitRequest reimbursementRequest = new ReimbursementSubmitRequest();
                reimbursementRequest.setReceiptId(pending.receiptId);
                reimbursementRequest.setUserId(user.getId());
                reimbursementRequest.setRequestedAmount(pending.amount);
                reimbursementRequest.setDescription(text);
                
                reimbursementService.submitReimbursement(reimbursementRequest);

                pendingReceipts.remove(chatId);
                
                sendMessage(chatId, 
                    """
                    Reimbursement request submitted successfully!
                    
                    Summary:
                    Merchant: %s
                    Amount: $%s
                    Description: %s
                    
                    Your request is now pending admin approval. You'll be notified once it's reviewed."""
                    .formatted(pending.merchantName, String.format("%.2f", pending.amount), text));
                
            } catch (Exception e) {
                sendMessage(chatId, 
                    """
                    Error submitting reimbursement: %s
                    
                    Please try again or contact support."""
                    .formatted(e.getMessage()));
                pendingReceipts.remove(chatId);
            }
            return;
        }
        
        if (text.equals("/start")) {
            sendMessage(chatId, 
                """
                Welcome to ReceiptHub!
                
                Send me a photo of your receipt to submit a reimbursement request.
                
                How it works:
                1. Upload receipt photo
                2. OCR will extract merchant name and amount
                3. Type a reimbursement description
                4. Request submitted, Wait for Admin Approval
                
                You'll be notified once your request is reviewed.""");
        } else {
            sendMessage(chatId, "Please send a photo of your receipt to submit a reimbursement request.\n\nUse /start for help.");
        }
    }
    
    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send Telegram message to chatId {}: {}", chatId, e.getMessage(), e);
        }
    }
}
