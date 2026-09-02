package com.stayworld.back.guesthouse.dto;

import com.stayworld.back.guesthouse.entity.Guesthouse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuesthouseDto {
    public long id;
    public String name;
    public int price;
    public String phoneNumber;
    public String address;
    public int capacity;
    public boolean parkingProvided;
    public boolean wifiProvided;
    public boolean breakfastProvided;
    public String introduction;
    public int visitorCount;
    public String music;

    public static GuesthouseDto from(Guesthouse entity){
        return new GuesthouseDto(
                entity.getId(),
                entity.getName(),
                entity.getPrice(),
                entity.getPhoneNumber(),
                entity.getAddress(),
                entity.getCapacity(),
                entity.isParkingProvided(),
                entity.isWifiProvided(),
                entity.isBreakfastProvided(),
                entity.getIntroduction(),
                entity.getVisitorCount(),
                entity.getMusic()
        );
    }
}
