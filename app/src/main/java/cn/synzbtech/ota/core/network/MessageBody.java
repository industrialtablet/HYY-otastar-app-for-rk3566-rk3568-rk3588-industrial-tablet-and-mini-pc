package cn.synzbtech.ota.core.network;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageBody {
    private int command;
    private Object data;
}
