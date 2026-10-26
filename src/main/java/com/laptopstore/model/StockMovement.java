
package com.laptopstore.model;

import java.time.LocalDateTime;

public class StockMovement {

    // 1. Thuoc tinh
    private int movementId;
    private int laptopId;
    private Integer accountId;
    private String movementType;
    private int quantity;
    private LocalDateTime createdAt;
    private String employeeName;
    // 2. Constructor khi tao giao dich moi
    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }
    public StockMovement(int laptopId,
                         Integer accountId,
                         String movementType,
                         int quantity) {

        setLaptopId(laptopId);
        setAccountId(accountId);
        setMovementType(movementType);
        setQuantity(quantity);
    }

    // Constructor dung enum StockMovementType (giu nguyen constructor String o tren)
    public StockMovement(int laptopId,
                         Integer accountId,
                         StockMovementType type,
                         int quantity) {

        this(laptopId, accountId,
                type == null ? null : type.name(),
                quantity);
    }

    // 3. Constructor khi doc tu database
    public StockMovement(int movementId,
                         int laptopId,
                         Integer accountId,
                         String movementType,
                         int quantity,
                         LocalDateTime createdAt) {

        this(laptopId, accountId, movementType, quantity);

        setMovementId(movementId);
        setCreatedAt(createdAt);
    }

    // 4. Getter va Setter

    public int getMovementId() {
        return movementId;
    }

    public void setMovementId(int movementId) {
        if (movementId < 0) {
            throw new IllegalArgumentException(
                    "Ma giao dich khong hop le"
            );
        }
        this.movementId = movementId;
    }

    public int getLaptopId() {
        return laptopId;
    }

    public void setLaptopId(int laptopId) {
        if (laptopId <= 0) {
            throw new IllegalArgumentException(
                    "Ma laptop phai lon hon 0"
            );
        }
        this.laptopId = laptopId;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        if (accountId != null && accountId <= 0) {
            throw new IllegalArgumentException(
                    "Ma tai khoan khong hop le"
            );
        }
        this.accountId = accountId;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        if (movementType == null ||
                (!movementType.equals("IMPORT") &&
                        !movementType.equals("EXPORT"))) {

            throw new IllegalArgumentException(
                    "Loai giao dich phai la IMPORT hoac EXPORT"
            );
        }

        this.movementType = movementType;
    }

    // Lay / gan loai giao dich duoi dang enum
    public StockMovementType getType() {
        return StockMovementType.fromString(movementType);
    }

    public void setType(StockMovementType type) {
        setMovementType(type == null ? null : type.name());
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "So luong phai lon hon 0"
            );
        }
        this.quantity = quantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // 5. Hien thi thong tin
    @Override
    public String toString() {
        return "StockMovement{" +
                "movementId=" + movementId +
                ", laptopId=" + laptopId +
                ", accountId=" + accountId +
                ", movementType='" + movementType + '\'' +
                ", quantity=" + quantity +
                ", createdAt=" + createdAt +
                '}';
    }
}
