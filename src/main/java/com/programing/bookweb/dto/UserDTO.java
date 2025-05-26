package com.programing.bookweb.dto;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    @Size(min = 2, message = "Họ và tên phải có ít nhất 2 ký tự")
    @Size(max = 50, message = "Họ và tên không được vượt quá 50 ký tự")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Họ và tên chỉ được chứa chữ cái và khoảng trắng")
    private String fullName;


    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", message = "Số điện thoại không đúng định dạng (VD: 0901234567)")
    private String phoneNumber;


    @Size(min = 10, message = "Địa chỉ phải có ít nhất 10 ký tự")
    @Size(max = 200, message = "Địa chỉ không được vượt quá 200 ký tự")
    private String address;


    private MultipartFile avatar;


    @Pattern(regexp = "^(Nam|Nữ|Không)$", message = "Giới tính chỉ được chọn: Nam, Nữ hoặc Không")
    private String gender;

    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

}
