package appllication.entity;

import appllication.model.auctionState.AuctionStatus;
import appllication.model.factory.DateFactory;
import appllication.model.auctionState.StatusProviderOfAnAuction;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


@Entity
public class Auction  {


    @GeneratedValue
    @Id
    private long id;

    @Email(message = "Email should be valid")
    private String emailAuthor;
    private String automaticOfferUser;

    private LocalDateTime publicationDate;
    private LocalDateTime finishDate;
    private LocalDateTime initialFinishDate;

    private long price;
    private long automaticOfferAmount;

    @Size(min = 10, max = 50,
            message = " title must be between 10 and 50 characters")
    private String title;

    @Size(min = 10, max = 100,
            message = " description must be between 10 and 50 characters")
    private String description;
    private String address;
    private String photos;

    private AuctionStatus currentState;

    @OneToMany(fetch= FetchType.EAGER,cascade=CascadeType.ALL)
    private List<Bidder> bidders;

    public Auction(){}


    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

    public List<Bidder> getBidders() { return bidders; }

    public void setBidders(List<Bidder> bidders) { this.bidders = bidders; }

    public String getEmailAuthor(){ return this.emailAuthor;    }

    public void setEmailAuthor(String emailAuthor) {
        this.emailAuthor = emailAuthor;
    }


    public long getAutomaticOfferAmount(){ return this.automaticOfferAmount;  }

    public void setAutomaticOfferAmount(long automaticOfferAmount) { this.automaticOfferAmount = automaticOfferAmount;    }



    public void setAutomaticOfferUser(String automaticOfferUser) {this.automaticOfferUser = automaticOfferUser; }



    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }


    public LocalDateTime getPublicationDate() {
        return publicationDate;
    }
    public void setPublicationDate(LocalDateTime aDate) {
        this.publicationDate = aDate;
    }


    public LocalDateTime getInitialFinishDate(){return this.initialFinishDate; }
    public void setInitialFinishDate(LocalDateTime initialFinishDate) {
        this.initialFinishDate = initialFinishDate;
    }

    public LocalDateTime getFinishDate() {
        return finishDate;
    }
    public void setFinishDate(LocalDateTime aDate) {
        this.finishDate = aDate;
    }

    public String getTitle() {        return title;}
    public void setTitle(String title) {        this.title = title;    }

    public String getDescription() {        return description;    }
    public void setDescription(String description) {        this.description = description;    }

    public String getPhotos() {        return photos;    }
    public void setPhotos(String photos) {     this.photos=photos;       }

    public String getAddress() {        return address;    }
    public void setAddress(String address) {        this.address = address;    }

    public AuctionStatus getCurrentState() {        return currentState;    }

    public void setCurrentState(AuctionStatus currentState) {        this.currentState = currentState;    }

    public AuctionStatus state() {

        this.setCurrentState(new StatusProviderOfAnAuction().getState(this));
        return this.getCurrentState();
    }

    private boolean theAuctionMustBeExtended() {
        return theOfferWasMadeWithinTheLastFiveMinutes() && !exceededFortyEightHours();
    }

    private boolean exceededFortyEightHours() {
        LocalDateTime fiveMinutesAfterTheFinishDate = getFinishDate().plusMinutes(5);
        LocalDateTime initialFinishDatePlusFortyEightHours= getInitialFinishDate().plusWeeks(2);
        return fiveMinutesAfterTheFinishDate.compareTo(initialFinishDatePlusFortyEightHours) > 0;
    }

    private boolean theOfferWasMadeWithinTheLastFiveMinutes() {

        LocalDateTime fiveMinutesBeforeTheCurrent = LocalDateTime.now().minusMinutes(1);

        return fiveMinutesBeforeTheCurrent.compareTo(this.finishDate) < 0;
    }

    private long fivePercentMoreThanTheCurrentPrice() {
        return (this.price * 5 / 100) + this.price;
    }


    public void offer() {
        long newPrice = fivePercentMoreThanTheCurrentPrice();
        setPrice(newPrice);
        if(theAuctionMustBeExtended()) {
            setFinishDate(this.finishDate.plusMinutes(5));

        }
    }

    public void automaticOffer(String email, long amount) {
        setAutomaticOfferUser(email);
        setAutomaticOfferAmount(amount);
    }

    public boolean thereIsToDoTheAutomaticOffer() {
        long newPrice = fivePercentMoreThanTheCurrentPrice();
        return this.automaticOfferAmount != 0 && newPrice <= this.getAutomaticOfferAmount();
    }

    public boolean thereIsAutomaticUser() {
      return !StringUtils.isEmpty(automaticOfferUser);
    }

    public boolean isFinished(){ return this.state().equals(AuctionStatus.COMPLETED); }


    public boolean isAuthor(String aBidder){ return  aBidder.equalsIgnoreCase(this.getEmailAuthor());}

    public boolean isInProgress(){return this.state().equals(AuctionStatus.IN_PROGRESS); }



}