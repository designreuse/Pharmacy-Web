package com.pharmacy.service.impl;

import com.pharmacy.domain.Article;
import com.pharmacy.domain.Pharmacy;
import com.pharmacy.repository.ArticleRepository;
import com.pharmacy.repository.PharmacyRepository;
import com.pharmacy.repository.search.ArticleSearchRepository;
import com.pharmacy.service.api.ImportService;
import org.apache.commons.lang.StringUtils;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.inject.Inject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexander on 14.11.2015.
 */
@Service
@Transactional
public class ImportServiceImpl implements ImportService {

    private final static Logger LOG = LoggerFactory.getLogger(ImportServiceImpl.class);

    @Inject
    private PharmacyRepository pharmacyRepository;
    @Inject
    private ArticleRepository articleRepository;
    @Inject
    private ArticleSearchRepository articleSearchRepository;

    /**
     * This method imports the articles from CSV file and save this into database.
     * The articles ware added to the a pharmacy.
     */
    @Override
    public void importCSVFile() {
        InputStream inputStream = null;
        Long PHARMACY_ID = 1004L;
        Pharmacy pharmacy = pharmacyRepository.findOne(PHARMACY_ID);
        try {
            inputStream = new FileInputStream("C:\\Users\\Alexander\\Dropbox\\Mappe1.csv");
            List<List<String>> csvList = parseCsv(inputStream, ';');
            Article article;
            assert csvList != null;
            for (List<String> attr : csvList) {
                article = getArticle(attr, pharmacy);
                Article saveArticle = articleRepository.save(article);
                articleSearchRepository.save(saveArticle);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * This method return a article from a line of CSV file.
     * @param attr is the line of csv file
     * @param pharmacy for assigned the the article.
     * @return article for save
     * @throws ServiceException
     */
    private Article getArticle(List<String> attr, Pharmacy pharmacy) throws ServiceException {
        String articleNumber = attr.get(0);
        Article article;
        if (StringUtils.isBlank(articleNumber)) {
            LOG.debug("number of article {} is empty", articleNumber);
            article = articleRepository.findArticleByArticleNumber(Integer.valueOf(articleNumber));
        } else {
            LOG.debug("number of article {} is not empty", articleNumber);
            article = new Article();
        }

        article.setArticelNumber(Integer.valueOf(articleNumber));
        article.setName(attr.get(2));
        article.setDescription(attr.get(3));
        article.setImageURL(attr.get(7));
        article.setDeepLink(attr.get(8));
        article.setKeyWords(attr.get(9));
        Assert.notNull(pharmacy);
        return article;
    }

    /**
     * CSV content parser. Convert an InputStream with the CSV contents to a
     * two-dimensional List of Strings representing the rows and columns of the
     * CSV. Each CSV record is expected to be separated by the specified CSV
     * field separator.
     *
     * @param inputStream The InputStream with the CSV contents.
     * @param csvSeparator The CSV field separator to be used.
     * @return A two-dimensional List of Strings representing the rows and
     * columns of the CSV.
     */
    private List<List<String>> parseCsv(InputStream inputStream, char csvSeparator) throws ServiceException {
        BufferedReader csvReader = null;
        try {
            List<List<String>> csvList = new ArrayList<>();
            csvReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String csvRecord;
            boolean fistLine = true;
            while ((csvRecord = csvReader.readLine()) != null) {
                if (fistLine) {
                    LOG.debug("ignore fist line of csv file becouse this is a header");
                    fistLine = false;
                } else {
                    LOG.trace("csvRecord={}", csvRecord);
                    csvList.add(parseCsvRecord(csvRecord, csvSeparator));
                }

            }
            return csvList;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (csvReader != null) {
                    csvReader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    /**
     * CSV record parser. Convert a CSV record to a List of Strings representing
     * the fields of the CSV record. The CSV record is expected to be separated
     * by the specified CSV field separator.
     *
     * @param record The CSV record.
     * @param csvSeparator The CSV field separator to be used.
     * @return A List of Strings representing the fields of each CSV record.
     */
    private List<String> parseCsvRecord(String record, char csvSeparator) {
        LOG.trace("Enter parseCsvRecord: record={}, csvSeparator={}", record, csvSeparator);
        // Prepare.
        boolean quoted = false;
        StringBuilder fieldBuilder = new StringBuilder();
        List<String> fields = new ArrayList<>();

        // Process fields.
        for (int i = 0; i < record.length(); i++) {
            char c = record.charAt(i);
            fieldBuilder.append(c);

            if (c == '"') {
                quoted = !quoted; // Detect nested quotes.
            }

            if ((!quoted && c == csvSeparator) // The separator ..
                    || i + 1 == record.length()) // .. or, the end of record.
            {
                String field = fieldBuilder.toString() // Obtain the field, ..
                        .replaceAll(csvSeparator + "$", "") // .. trim ending separator, ..
                        .replaceAll("^\"|\"$", "") // .. trim surrounding quotes, ..
                        .replace("\"\"", "\""); // .. and un-escape quotes.
                fields.add(field.trim()); // Add field to List.
                fieldBuilder = new StringBuilder(); // Reset.
            }
        }
        LOG.trace("Exit parseCsvRecord: fields={}", fields);
        return fields;
    }
}