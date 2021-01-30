package com.android.pavanblog;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<BlogPost> bloglist;
    public Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public BlogRecyclerAdapter(List<BlogPost> bloglist){
        this.bloglist = bloglist;


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item_view,parent,false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final String blogPostId = bloglist.get(position).BlogPostId;
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();

        String desc_data = bloglist.get(position).getDesc();
        holder.setDesctext(desc_data);

        final String image_url = bloglist.get(position).getImage_url();
        holder.setBlogImage(image_url);

        String user_id = bloglist.get(position).getUser_id();
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    String username = task.getResult().getString("name");
                    String image_user = task.getResult().getString("image");

                    holder.blog_user_data(username,image_user);
                }
            }
        });




        long millSeconds = bloglist.get(position).getTimestamp().getTime();
        String dateString = DateFormat.format("dd/MM/yyyy",new Date(millSeconds)).toString();
        holder.setTime(dateString);

        //Likes Count
        firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (!queryDocumentSnapshots.isEmpty()){
                    int count = queryDocumentSnapshots.size();
                    holder.updateLikeCount(count);
                }
                else{
                    holder.updateLikeCount(0);
                }

            }
        });


        firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (documentSnapshot.exists()){
                    holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.drawable.likebtn_pink_foreground));
                }else{
                    holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.drawable.likebtn_foreground));
                }


            }
        });


        holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (!task.getResult().exists()){
                            Map<String,Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(currentUserId).set(likesMap);
                        }else{
                            firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(currentUserId).delete();
                        }
                    }
                });




            }
        });

        holder.commentImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent CommentIntent = new Intent(context,CommentActivity.class);
                context.startActivity(CommentIntent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return bloglist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private ImageView blog_image_view;
        private TextView desc_blog;
        private TextView blogTime;
        private TextView username;
        private CircleImageView blogUserImage;
        private ImageView commentImage;

        private ImageView blogLikeBtn;
        private TextView blogLikeCount;




        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            blogLikeBtn = mView.findViewById(R.id.likeimg);
            blogLikeCount = mView.findViewById(R.id.likesCount);
            commentImage = mView.findViewById(R.id.commentImg);


        }
        public void setDesctext(String desctext){
            desc_blog = mView.findViewById(R.id.blog_desc);
            desc_blog.setText(desctext);
        }

        public void setBlogImage(String downloadUri){

            blog_image_view = mView.findViewById(R.id.postImage);
            Glide.with(context).load(downloadUri).into(blog_image_view);

        }

        public void setTime(String time){

            blogTime = mView.findViewById(R.id.blog_post_date);
            blogTime.setText(time);

        }
        public void blog_user_data(String name,String image){
            blogUserImage = mView.findViewById(R.id.blog_user_img);
            username = mView.findViewById(R.id.username_post);
            username.setText(name);
            Glide.with(context).load(image).into(blogUserImage);

        }
        public void updateLikeCount(int Count){

            blogLikeCount = mView.findViewById(R.id.likesCount);
            blogLikeCount.setText(Count+" Likes");

        }


    }
}
