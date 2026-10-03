package ru.yandex.practicum.market.web.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.dto.ItemsAndPagingDto;
import ru.yandex.practicum.market.service.CartService;
import ru.yandex.practicum.market.web.request.ItemCreateRequest;
import ru.yandex.practicum.market.web.request.ItemSearchRequest;
import ru.yandex.practicum.market.web.request.PostItemRequest;
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
    public Mono<String> getItems(
            @Valid @ModelAttribute ItemSearchRequest searchDto,
            Model model) {

        return itemService.search(
                        searchDto.getSearch(),
                        searchDto.toPageable()
                )
                .map(itemsAndPagingDto ->
                {var groupedItems = groupByNumber(
                        itemsAndPagingDto.items(),
                                    3,
                                    () -> new ItemDto(
                                            -1, null, null, null, null, 0
                                    )
                            );

                    model.addAttribute("items", groupedItems);
                    model.addAttribute("search", searchDto.getSearch());
                    model.addAttribute("sort", searchDto.getSort().name());
                    model.addAttribute(
                            "paging",
                            new PagingView(
                                    searchDto.getPageSize(),
                                    searchDto.getPageNumber(),
                                    itemsAndPagingDto.hasPrevious(),
                                    itemsAndPagingDto.hasNext()
                            )
                    );

                            return "items";

                        });
    }

    @PostMapping
    public Mono<String> postItems(
            @Valid @ModelAttribute PostItemRequest postItemRequest) {

        var mono = switch (postItemRequest.action()) {
            case PLUS -> cartService.addToCart(postItemRequest.id());
            case MINUS -> cartService.removeFromCart(postItemRequest.id());
        };

        return mono.then(Mono.just("redirect:/items?search=%s&sort=%s&pageNumber=%s&pageSize=%s"
                .formatted(postItemRequest.search(), postItemRequest.sort().name(), postItemRequest.pageNumber(), postItemRequest.pageSize())));
    }

    @GetMapping("/new")
    public Mono<String> getNewItem(Model model) {
        model.addAttribute("itemCreateRequest", new ItemCreateRequest());
        return Mono.just("item-new");
    }

    @PostMapping("/new")
    public Mono<String> postNewItem(
            @Valid @ModelAttribute("itemCreateRequest") ItemCreateRequest itemCreateRequest,
            BindingResult bindingResult,
            @RequestPart("image") FilePart image) {

        if (bindingResult.hasErrors()) {
            return Mono.just("item-new");
        }

        return itemService.create(itemCreateRequest, image).map(it -> "redirect:/items/%s".formatted(it.id()));
    }

    @GetMapping("/{id}")
    public Mono<String> getItem(@PathVariable long id, Model model) {

        return itemService.retrieveById(id).map( it ->
                model.addAttribute("item", it)
        ).then(Mono.just("item"));
    }

    @PostMapping("/{id}")
    public Mono<String> postItem(@PathVariable("id") long id, @Valid @ModelAttribute ActionDto action, Model model) {

        var mono = switch (action.action) {
            case PLUS -> cartService.addToCart(id);
            case MINUS -> cartService.removeFromCart(id);
        };

        return mono.then(itemService.retrieveById(id)).doOnNext(it ->
                model.addAttribute("item", it)
        ).then(Mono.just("item"));

    }

    public record ActionDto(PostItemsAction action) {}
}
