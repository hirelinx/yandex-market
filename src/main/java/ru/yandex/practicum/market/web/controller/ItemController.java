package ru.yandex.practicum.market.web.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.dto.ItemsAndPagingDto;
import ru.yandex.practicum.market.service.CartService;
import ru.yandex.practicum.market.web.request.ItemCreateRequest;
import ru.yandex.practicum.market.web.request.ItemSearchRequest;
import ru.yandex.practicum.market.web.request.PostItemsAction;
import ru.yandex.practicum.market.web.view.PagingView;
import ru.yandex.practicum.market.service.ItemService;

import static ru.yandex.practicum.market.common.ListUtils.groupByNumber;

@Controller
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final CartService cartService;

    @GetMapping
    public String getItems(
            @Valid @ModelAttribute ItemSearchRequest searchDto,
            Model model) {

        ItemsAndPagingDto itemsAndPagingDto = itemService.search(searchDto.getSearch(), searchDto.toPageable());

        var groupedItems = groupByNumber(itemsAndPagingDto.items(), 3, () -> new ItemDto(
                        -1, null, null, null, null, 0
                ));

        model.addAttribute("items", groupedItems);
        model.addAttribute("search", searchDto.getSearch());
        model.addAttribute("sort", searchDto.getSort().name());
        model.addAttribute("paging",
                new PagingView(
                        searchDto.getPageSize(),
                        searchDto.getPageNumber(),
                        itemsAndPagingDto.hasPrevious(),
                        itemsAndPagingDto.hasNext()
                )
        );
        return "items";
    }

    @PostMapping
    public String postItems(
            @RequestParam(name = "id") long itemId,
            @Valid @ModelAttribute ItemSearchRequest searchDto,
            @RequestParam(name = "action") PostItemsAction postItemsAction) {

        switch (postItemsAction) {
            case PLUS -> cartService.addToCart(itemId);
            case MINUS -> cartService.removeFromCart(itemId);
        }

        return "redirect:/items?search=%s&sort=%s&pageNumber=%s&pageSize=%s"
                .formatted(searchDto.getSearch(), searchDto.getSort().name(), searchDto.getPageNumber(), searchDto.getPageSize());
    }

    @GetMapping("/new")
    public String getNewItem(Model model) {
        model.addAttribute("itemCreateRequest", new ItemCreateRequest());
        return "item-new";
    }

    @PostMapping("/new")
    public String postNewItem(
            @Valid @ModelAttribute("itemCreateRequest") ItemCreateRequest itemCreateRequest,
            @RequestParam("image") MultipartFile image,
            BindingResult bindingResult) {

        if (image.isEmpty()) {
            bindingResult.reject("image.required", "Изображение обязательно");
        }

        if (bindingResult.hasErrors()) {
            return "item-new";
        }

        ItemDto item = itemService.create(itemCreateRequest, image);
        return "redirect:/items/%s".formatted(item.id());
    }

    @GetMapping("/{id}")
    public String getItem(@PathVariable long id, Model model) {

        var item = itemService.retrieveById(id);

        model.addAttribute("item", item);
        return "item";
    }

    @PostMapping("/{id}")
    public String postItem(@PathVariable long id, @RequestParam(name = "action") PostItemsAction postItemsAction, Model model) {

        switch (postItemsAction) {
            case PLUS -> cartService.addToCart(id);
            case MINUS -> cartService.removeFromCart(id);
        }

        var item = itemService.retrieveById(id);
        model.addAttribute("item", item);
        return "item";
    }
}
