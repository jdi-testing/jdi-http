package com.epam.jdi.httptests.examples.custom;

import com.epam.jdi.dto.Board;
import com.epam.jdi.dto.Card;
import com.epam.jdi.dto.TrelloList;
import com.epam.jdi.httptests.TrelloService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;


import static com.epam.http.requests.RequestDataFacrtory.pathParams;
import static com.epam.http.requests.ServiceInit.init;
import static com.epam.jdi.httptests.utils.TrelloDataGenerator.generateBoard;
import static com.epam.jdi.httptests.utils.TrelloDataGenerator.generateCard;
import static com.epam.jdi.httptests.utils.TrelloDataGenerator.generateList;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.testng.Assert.assertEquals;

public class PreconditionParallelTests {
    public TrelloService service;
    public static final String CSV_DATA_FILE = "src/test/resources/testWithPreconditions.csv";

    @BeforeClass
    public void initService() {
        service = init(TrelloService.class);
    }

    @DataProvider(name = "createNewBoards", parallel = true)
    public static Object[][] createNewBoards() {
        return new Object[][] {
                {generateBoard()},
                {generateBoard()},
                {generateBoard()}
        };
    }

    @Test(dataProvider = "createNewBoards", threadPoolSize = 3)
    public void createCardInBoard1(Board board) {
        //Crate board
        Board createdBoard = service.createBoard(board);
        Board gotBoard = service.getBoard(createdBoard.id);
        assertEquals(gotBoard.name, createdBoard.name, "Name of created board is incorrect");

        //Create list
        TrelloList tList = generateList(createdBoard);
        TrelloList createdList = service.createList(tList);

        //Create Card
        Card card = generateCard(createdBoard, createdList);
        Card createdCard = service.addNewCardToBoard(card);

        //Check that card was added
        Board cardBoard = service.getCardBoard(createdCard.id);
        assertEquals(cardBoard.name, board.name, "Card wasn't added to board");
    }

    @DataProvider(name = "dataProviderFromCSV", parallel = true)
    public static Object[] dataProviderFromCSV() throws IOException {
        Reader in = new FileReader(CSV_DATA_FILE);
        Iterable<CSVRecord> records = CSVFormat.DEFAULT
                .withHeader("id", "name", "shortUrl", "url")
                .withFirstRecordAsHeader()
                .parse(in);
        ArrayList<Object[]> dataList = new ArrayList<>();
        for (CSVRecord record : records) {
            dataList.add(new Object[] {record.get(0),record.get(1),record.get(2),record.get(3)});
        }
        return dataList.toArray(new Object[dataList.size()][]);
    }

    @Test (dataProvider = "dataProviderFromCSV", threadPoolSize = 3)
    public void getBoardTestWithRequestData(String boardId, String expectedName, String expectedShortUrl, String expectedUrl) {
        service.getBoardById.call(pathParams().add("board_id", boardId))
                .isOk().assertThat().body("name", equalTo(expectedName))
                .body("shortUrl",equalTo(expectedShortUrl))
                .body("url",equalTo(expectedUrl));
    }
}