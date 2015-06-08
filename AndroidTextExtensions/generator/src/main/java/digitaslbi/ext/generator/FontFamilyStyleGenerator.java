/*
 * Copyright (c) 2015 DigitasLBi.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package digitaslbi.ext.generator;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.io.CharStreams;
import digitaslbi.ext.common.Font;
import digitaslbi.ext.common.FontFamily;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;

import static com.google.common.collect.Iterables.transform;


/**
 * Represents a code {@link CodeGenerator} which generates an `XML` resource representing an
 * `android:TextAppearance` style definition for a {@link FontFamily}.
 */
public class FontFamilyStyleGenerator implements CodeGenerator {


    protected FontFamilyStyleGenerator(Builder builder) {
    }

    public static class Builder extends AbstractBuilder<FontFamilyStyleGenerator, Builder> {

        @Override
        public Builder self() {
            return this;
        }

        @Override
        public FontFamilyStyleGenerator build() {
            return new FontFamilyStyleGenerator(this);
        }
    }

    @Override
    public List<SimpleImmutableEntry<String, String>> generate(final List<FontFamily> fontFamilies) {
        return FluentIterable.from(fontFamilies).transform(new Function<FontFamily, SimpleImmutableEntry<String, String>>() {
            @Override public SimpleImmutableEntry<String, String> apply(FontFamily input) {
                try {
                    return generate(input);
                } catch (Exception e) {
                    return null;
                }
            }
        }).filter(new Predicate<SimpleImmutableEntry<String, String>>() {
            @Override public boolean apply(SimpleImmutableEntry<String, String> input) {
                return input != null;
            }
        }).toList();
    }

    @Override
    public SimpleImmutableEntry<String, String> generate(final FontFamily fontFamily) {
        try {
            final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            final Element resources = doc.createElement("resources");
            doc.appendChild(resources);
            resources.appendChild(doc.createComment("DO NOT MODIFY THIS FILE. Generated by Android Text Extensions plugin."));

            if (!fontFamily.hasFontWithSameName()) {
                Element style = doc.createElement("style");
                style.setAttribute("name", fontFamily.getName());
                style.setAttribute("parent", "android:TextAppearance");
                resources.appendChild(style);
            }

            final Iterable<Element> result = transform(fontFamily.getFonts(), new Function<Font, Element>() {
                @Override
                public Element apply(Font input) {
                    return appendStyle(doc, fontFamily, input);
                }
            });

            for (Element element : result) {
                resources.appendChild(element);
            }

            final StringBuilder appendable = new StringBuilder();
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(new DOMSource(doc), new StreamResult(CharStreams.asWriter(appendable)));
            return new SimpleImmutableEntry<String, String>(fontFamily.getName() + ".xml", appendable.toString());

        } catch (ParserConfigurationException e) {
            return null;
        } catch (TransformerConfigurationException e) {
            return null;
        } catch (TransformerException e) {
            return null;
        }
    }

    protected Element appendStyle(Document doc, FontFamily fontFamily, Font font) {
        final String styleName = font.getStyleName();
        Element style = doc.createElement("style");
        style.setAttribute("name", styleName);

        if (styleName.equals(fontFamily.getName())) {
            style.setAttribute("parent", "android:TextAppearance");
        }

        Element item = doc.createElement("item");
        item.setAttribute("name", "android:fontFamily");
        item.setTextContent(styleName);

        style.appendChild(item);
        return style;
    }
}
