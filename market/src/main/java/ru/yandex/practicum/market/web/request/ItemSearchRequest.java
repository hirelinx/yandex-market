package ru.yandex.practicum.market.web.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemSearchRequest {
    String search = "";
    ItemSorting sort = ItemSorting.NO;
    int pageNumber = 1;
    int pageSize = 5;

    public Pageable toPageable() {
        return  sort == ItemSorting.NO
                ? PageRequest.of(pageNumber - 1, pageSize)
                : PageRequest.of(
                pageNumber,
                pageSize - 1,
                Sort.by(sort == ItemSorting.ALPHA ? "title" : "price").ascending()
        );
    }
}
