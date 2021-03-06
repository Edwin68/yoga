package org.skyscreamer.yoga.listener;

import org.skyscreamer.yoga.model.MapHierarchicalModel;
import org.skyscreamer.yoga.selector.Property;
import org.skyscreamer.yoga.selector.Selector;
import org.skyscreamer.yoga.selector.parser.GDataSelectorParser;
import org.skyscreamer.yoga.selector.parser.LinkedInSelectorParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class NavigationLinksListener implements RenderingListener
{
    private HrefListener _hrefListener = new HrefListener();

    public void setHrefListener( HrefListener hrefListener )
    {
        this._hrefListener = hrefListener;
    }

    @Override
    public void eventOccurred( RenderingEvent event )
    {
        Selector selector = event.getSelector();
        if (event.getType() != RenderingEventType.POJO_CHILD || selector.isInfluencedExternally())
        {
            return;
        }

        Class<?> instanceType = event.getValueType();
        Object instance = event.getValue();

        String urlSuffix = event.getRequestContext().getUrlSuffix();

        MapHierarchicalModel<?> navigationLinks = ((MapHierarchicalModel<?>) event.getModel())
                .createChildMap( "navigationLinks" );
        Collection<Property> fieldNames = getNonSelectedFields( selector, instanceType, instance );

        for (Property field : fieldNames)
        {
            String fieldName = field.name();
            MapHierarchicalModel<?> navModel = navigationLinks.createChildMap( fieldName );
            String format;
            if (event.getRequestContext().getSelectorParser() instanceof LinkedInSelectorParser) {
                format = "%s?selector=:(%s)";
            }
            else if (event.getRequestContext().getSelectorParser() instanceof GDataSelectorParser) {
                format = "%s?selector=%s";
            }
            else {
                throw new IllegalStateException("Unknown selector type: " + event.getRequestContext().getSelectorParser().getClass().getName());
            }
            String hrefSuffixAndSelector = String.format( format, urlSuffix, fieldName );
            _hrefListener.addUrl( instance, instanceType, hrefSuffixAndSelector, navModel,
                    event.getRequestContext() );
            navModel.addProperty( "name", fieldName );
        }
    }

    public Collection<Property> getNonSelectedFields( Selector selector, Class<?> instanceType,
            Object instance )
    {
        Collection<Property> fieldNames = new ArrayList<Property>(
                selector.getAllPossibleFields( instanceType ) );
        Iterable<Property> selectedFields = selector.getSelectedFields( instanceType, instance );
        for (Iterator<Property> iterator = fieldNames.iterator(); iterator.hasNext();)
        {
            Property property = iterator.next();
            for (Property selected : selectedFields)
            {
                if (selected.name().equals( property.name() ))
                {
                    iterator.remove();
                }
            }
        }
        return fieldNames;
    }
}
