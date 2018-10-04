package com.example.lively;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

//The adapter of the event list
public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsViewHolder> {
    private List<Event> eventslist;
    final private EventsClickListener eventsClickListener;


    public EventsAdapter(Context context, List<Event> eventslist, EventsClickListener eventsClickListener) {
        this.eventslist = eventslist;
        this.eventsClickListener = eventsClickListener;


    }

    @Override
    public EventsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.events_list_item, viewGroup, false);
        EventsViewHolder viewHolder = new EventsViewHolder(view);
        return viewHolder;
    }


    public interface EventsClickListener {
        void OnListItemClick(int clickedItemPosition);

    }

    @Override
    public void onBindViewHolder(EventsViewHolder holder, int position) {

        Event event = eventslist.get(position);
        holder.nameplacetv.setText(event.getArtistName() + " at " + event.getHostName());
        holder.genretv.setText(event.getGenre());
        holder.datetv.setText(event.getDateTime());
        if (event.getPrice().equals("0")) {
            holder.priceTv.setText("Free");
        } else
            holder.priceTv.setText(event.getPrice() + " $");
    }

    @Override
    public int getItemCount() {

        return eventslist.size();

    }

    class EventsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView nameplacetv;
        protected TextView datetv;
        protected TextView genretv;
        protected TextView priceTv;

        public EventsViewHolder(View view) {
            super(view);
            nameplacetv = view.findViewById(R.id.name_and_place_id_item);
            datetv = view.findViewById(R.id.date_item_list_tv);
            genretv = view.findViewById(R.id.genre_item_list_tv);
            priceTv = view.findViewById(R.id.price_fee_item_list_tv);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            eventsClickListener.OnListItemClick(clickedPosition);
        }
    }
}
