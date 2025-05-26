package com.programing.bookweb.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    @NotEmpty(message = "Tiêu đề không được để trống")
    private String title;

    @NotEmpty(message = "Tác giả không được để trống")
    private String author;

    private String translator;

    private MultipartFile imageProduct;

    @NotEmpty(message = "Nhà cung cấp không được để trống")
    private String supplier;

    @NotEmpty(message = "Nhà phát hành không được để trống")
    private String publisher;

    @Min(0)
    @Max(2025)
    private int publishingYear;

    @NotNull(message = "Thể loại không được để trống")
    private Long categoryId;

    @Size(min = 100, message = "Phần giới thiệu phải dài ít nhất 100 ký tự")
    @Size(max = 10000, message = "Phần giới thiệu dài không quá 10000 ký tự")
    private String introduction;

    @NotNull(message = "Giá tiền không được để trống")
    @Min(0)
    private double price;

    @Min(0)
    @Max(100)
    private double discount;

    @Min(0)
    private double weight;

    @Min(0)
    private int numberOfPage;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1)
    private int quantity;

    @NotEmpty(message = "Kích thước không được để trống")
    private String size;

    @NotEmpty(message = "Hình thức không được để trống")
    private String layout;
}
