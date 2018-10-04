package com.example.lively;

import android.os.Parcel;
import android.os.Parcelable;
//Event class ,parcalable to sent objects around with intents easier

public class Event implements Parcelable {
    private String artistName;
    private String hostName;
    private String address;
    private String city;
    private String phoneNumber;

    public Event() {

    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getEntrancePrice() {
        return entrancePrice;
    }

    public void setEntrancePrice(String entrancePrice) {
        this.entrancePrice = entrancePrice;
    }

    public String getArtistComment() {
        return artistComment;
    }

    public void setArtistComment(String artistComment) {
        this.artistComment = artistComment;
    }

    private double longitude;
    private double langitude;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLangitude() {
        return langitude;
    }

    public void setLangitude(double langitude) {
        this.langitude = langitude;
    }

    private String dateTime;
    private String genre;
    private String entrancePrice;
    private String artistComment;
    private String price;

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    protected Event(Parcel in) {
        artistName = in.readString();
        hostName = in.readString();
        address = in.readString();
        city = in.readString();
        phoneNumber = in.readString();
        longitude = in.readDouble();
        langitude = in.readDouble();
        dateTime = in.readString();
        genre = in.readString();
        price = in.readString();
        artistComment = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artistName);
        dest.writeString(hostName);
        dest.writeString(address);
        dest.writeString(city);
        dest.writeString(phoneNumber);
        dest.writeDouble(longitude);
        dest.writeDouble(langitude);
        dest.writeString(dateTime);
        dest.writeString(genre);
        dest.writeString(price);
        dest.writeString(artistComment);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
}