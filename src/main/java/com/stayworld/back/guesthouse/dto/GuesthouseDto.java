package com.stayworld.back.guesthouse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuesthouseDto {
    public int id;
    public String name;
    public int price;
    public String phoneNumber;
    public String address;
    public int capacity;
    public boolean parking;
    public boolean wifi;
    public boolean breakfast;
    public String introduction;
    public int visitorCount;
}
