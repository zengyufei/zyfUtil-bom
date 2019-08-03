package com.zyf.test.squirrel.domain;

import com.zyf.test.squirrel.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private Integer id;

    private OrderStatus orderStatus;

}
