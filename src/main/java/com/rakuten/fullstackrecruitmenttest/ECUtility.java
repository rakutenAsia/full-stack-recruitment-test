package com.rakuten.fullstackrecruitmenttest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ECUtility {

    private static Random random = new Random(26447L);

    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Halting the Utility. On next files will be Truncated!");
        }));

        if (args.length < 1) {
            System.out.println("Please provide Directory to Generate file.");
            System.exit(0);
        }

        Path fileLocation = Paths.get(args[0]);
        if (!Files.exists(fileLocation)) {
            System.out.println("Folder Doesn't Exist.");
            System.out.flush();
            System.exit(0);
        }
        new ECUtility().generateFiles(fileLocation);

    }

    private void generateFiles(Path fileLocation) {

        AdType[] addTypes = { new AdType(1, 10), new AdType(2, 5), new AdType(3, 3), new AdType(4, 15) };

        int[] merchants = random.ints(50, 1231, 1888).toArray();

        Supplier<AdType> randomAdType = () -> addTypes[random.nextInt(addTypes.length)];

        Supplier<Integer> randomMerchantId = () -> merchants[random.nextInt(merchants.length)];

        IntSupplier randomCampaignId = () -> random.nextInt(10000);

        Supplier<Integer> randomBduget = () -> (1 + random.nextInt(9)) * 1000;

        Supplier<Set<String>> randomItemSet = () -> random.ints(random.nextInt(100) + 40, 100000, 1000000).boxed()
                .map(this::createItemId).collect(Collectors.toSet());

        Function<Integer, Campaign> randomCampaignForId = id -> new Campaign(createCampaignId(id),
                createMerchantId(randomMerchantId.get()), randomBduget.get(), randomAdType.get(), randomItemSet.get());

        Campaign[] campaigns = IntStream.generate(randomCampaignId).limit(200).boxed().map(randomCampaignForId)
                .toArray(Campaign[]::new);

        try {
            generateAdFee(fileLocation, addTypes);
            generateCampaignsCsv(fileLocation, campaigns);
            generateItemsCsv(fileLocation, campaigns);
            generateClicks(fileLocation, campaigns);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void generateAdFee(Path fileLocation, AdType[] adTypes) throws IOException {
        Path path = Paths.get(fileLocation.toString(), "adFee.csv");
        Files.write(path, Arrays.stream(adTypes).map(c -> String.format("%s,%s", c.getAdTypeId(), c.getAdFee()))
                .collect(Collectors.toList()), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        System.out.println("Ad Fees File generated : " + path.toUri().toString());
    }

    public void generateCampaignsCsv(Path fileLocation, Campaign[] campaigns) throws IOException {
        Path path = Paths.get(fileLocation.toString(), "campaigns.csv");
        Files.write(path, Arrays.stream(campaigns).map(this::toCsv).collect(Collectors.toList()),
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        System.out.println("Campaigns File generated : " + path.toUri().toString());
    }

    public void generateItemsCsv(Path fileLocation, Campaign[] campaigns) throws IOException {
        Path path = Paths.get(fileLocation.toString(), "items.csv");
        Files.write(path,
                Arrays.stream(campaigns)
                        .flatMap(
                                c -> c.getItems().stream().map(item -> String.format("%s,%s", c.getCampaignId(), item)))
                        .collect(Collectors.toList()),
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        System.out.println("Items File generated : " + path.toUri().toString());
    }

    public void generateClicks(Path fileLocation, Campaign[] campaigns) throws IOException, InterruptedException {
        Path path = Paths.get(fileLocation.toString(), "clicks.csv");
        Files.deleteIfExists(path);
        int clickCount = 0;
        System.out.println("Generating Click data at : " + path.toUri().toString());
        while (true) {
            clickCount = appendClicks(path, campaigns, clickCount);
            TimeUnit.MILLISECONDS.sleep(533L);
        }
    }

    private int appendClicks(Path path, Campaign[] campaigns, int clickCount) throws IOException {
        Campaign campaign = campaigns[random.nextInt(campaigns.length)];
        String itemId = campaign.getItems().stream().skip(random.nextInt(campaign.getItems().size() - 1)).findFirst()
                .get();
        Click click = new Click(120_000_000_000L + clickCount++, LocalDateTime.now(), itemId,
                campaign.getAdType().getAdTypeId());
        Files.write(path, Stream.of(click).map(this::toCsv).collect(Collectors.toList()), StandardOpenOption.APPEND,
                StandardOpenOption.CREATE);
        System.out.printf("Click (%s,%s,%s, %s) generated. %n", click.getClickId(), click.getClickedTime(),
                click.getItemId(), click.getAdType());
        return clickCount;
    }

    public String toCsv(Click c) {
        return String.format("%s,%s,%s,%s", c.getClickId(),
                c.getClickedTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), c.getItemId(), c.getAdType());
    }

    public String toCsv(Campaign c) {
        return String.format("%s,%s,%s,%s", c.getMerchantId(), c.getCampaignId(), c.getBudget(),
                c.getAdType().getAdTypeId());
    }

    public String createCampaignId(Integer id) {
        return String.format("C%06d", id);
    }

    public String createMerchantId(Integer id) {
        return String.format("M%06d", id);
    }

    public String createItemId(Integer id) {
        return String.format("I%06d", id);
    }

    private class AdType {

        public AdType(Integer adTypeId, Integer adFee) {
            super();
            this.adTypeId = adTypeId;
            this.adFee = adFee;
        }

        private Integer adTypeId;
        private Integer adFee;

        public Integer getAdTypeId() {
            return adTypeId;
        }

        public Integer getAdFee() {
            return adFee;
        }

    }

    private class Campaign {

        public Campaign(String campaignId, String merchantId, Integer budget, AdType adType, Set<String> items) {
            super();
            this.campaignId = campaignId;
            this.merchantId = merchantId;
            this.budget = budget;
            this.adType = adType;
            this.items = items;
        }

        private String      campaignId;
        private String      merchantId;
        private Integer     budget;
        private AdType      adType;
        private Set<String> items;

        public String getCampaignId() {
            return campaignId;
        }

        public String getMerchantId() {
            return merchantId;
        }

        public Integer getBudget() {
            return budget;
        }

        public AdType getAdType() {
            return adType;
        }

        public Set<String> getItems() {
            return items;
        }

    }

    private class Click {

        public Click(Long clickId, LocalDateTime clickedTime, String itemId, Integer adType) {
            super();
            this.clickId = clickId;
            this.clickedTime = clickedTime;
            this.itemId = itemId;
            this.adType = adType;
        }

        private Long          clickId;
        private LocalDateTime clickedTime;
        private String        itemId;
        private Integer       adType;

        public Long getClickId() {
            return clickId;
        }

        public LocalDateTime getClickedTime() {
            return clickedTime;
        }

        public String getItemId() {
            return itemId;
        }

        public Integer getAdType() {
            return adType;
        }

    }
}
