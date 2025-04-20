package tochka;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class run {
    private static final String CHECK_IN = "check-in";
    private static final String CHECK_OUT = "check-out";


    public static boolean checkCapacity(int maxCapacity, List<Map<String, String>> guests) {
        List<Event> events = getEvents(guests);

        List<Event> sortedEvents = sortEvents(events);

        int currentGuests = 0;
        for (Event event : sortedEvents) {
            currentGuests += event.change;
            if (currentGuests > maxCapacity) {
                return false;
            }
        }

        return true;
    }

    /**
     * Преобразует список гостей в список событий заезда и выезда.
     */
    private static List<Event> getEvents(List<Map<String, String>> guests) {
        List<Event> events = new ArrayList<>();

        for (Map<String, String> guest : guests) {
            LocalDate checkIn = LocalDate.parse(guest.get(CHECK_IN));
            LocalDate checkOut = LocalDate.parse(guest.get(CHECK_OUT));

            events.add(new Event(checkIn, 1));
            events.add(new Event(checkOut, -1));
        }

        return events;
    }

    /**
     * Сортирует список событий по возрастанию даты. При совпадении дат выезд
     * обрабатывается раньше заезда.
     *
     * @param events неотсортированный список событий
     * @return новый список событий, отсортированный по дате и типу изменения
     */
    private static List<Event> sortEvents(List<Event> events) {
        return events.stream()
                .sorted(
                        Comparator.comparing((Event e) -> e.date)
                                .thenComparing(e -> e.change)
                ).toList();
    }

    /**
     * Приватный вложенный класс, описывающий событие изменения числа занятых номеров.
     * Содержит дату и величину изменения: +1 для заезда, -1 для выезда.
     */
    private static class Event {
        LocalDate date;
        int change;

        Event(LocalDate date, int change) {
            this.date = date;
            this.change = change;
        }
    }


    // Вспомогательный метод для парсинга JSON строки в Map
    private static Map<String, String> parseJsonToMap(String json) {
        Map<String, String> map = new HashMap<>();
        // Удаляем фигурные скобки
        json = json.substring(1, json.length() - 1);


        // Разбиваем на пары ключ-значение
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            String key = keyValue[0].trim().replace("\"", "");
            String value = keyValue[1].trim().replace("\"", "");
            map.put(key, value);
        }

        return map;
    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);


        // Первая строка - вместимость гостиницы
        int maxCapacity = Integer.parseInt(scanner.nextLine());


        // Вторая строка - количество записей о гостях
        int n = Integer.parseInt(scanner.nextLine());


        List<Map<String, String>> guests = new ArrayList<>();


        // Читаем n строк, json-данные о посещении
        for (int i = 0; i < n; i++) {
            String jsonGuest = scanner.nextLine();
            // Простой парсер JSON строки в Map
            Map<String, String> guest = parseJsonToMap(jsonGuest);
            guests.add(guest);
        }


        // Вызов функции
        boolean result = checkCapacity(maxCapacity, guests);


        // Вывод результата
        System.out.println(result ? "True" : "False");


        scanner.close();
    }
}