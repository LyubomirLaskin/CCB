package app.ccb.domain.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "clients")
public class Client extends BaseEntity {

    private String fullName;
    private Integer age;
    private BankAccount bankAccount;

    public Client() {
    }

    @Column(name = "full_name", nullable = false)
    @NotNull
    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Column(name = "age")
    public Integer getAge() {
        return this.age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @OneToOne(targetEntity = BankAccount.class)
    @JoinColumn(name = "bank_account")
    public BankAccount getBankAccount() {
        return this.bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }
}
