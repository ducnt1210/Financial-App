package com.example.financialapp;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.financialapp.Model.UserModel;
import com.example.financialapp.databinding.ActivityProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ProfileActivity extends AppCompatActivity {
    ActivityProfileBinding binding;
    UserModel tempUser;
    FirebaseStorage storage;

    SweetAlertDialog sweetAlertDialog;
    Uri tempImage;
    ActivityResultLauncher<String> getImage = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result != null) {
                        binding.imageProfile.setImageURI(result);
                        tempImage = result;
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());

        storage = FirebaseStorage.getInstance();

        tempUser = MainActivity.currentUser;

        binding.userName.setText(tempUser.getName());
        binding.userNumber.setText(tempUser.getNumber());
        binding.userEmail.setText(tempUser.getEmail());
        binding.imageProfile.setImageResource(R.drawable.default_profile_picture);

        if (MainActivity.profilePicture != null) {
            Glide.with(ProfileActivity.this)
                    .load(MainActivity.profilePicture)
                    .into(binding.imageProfile);
        }

        binding.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseFirestore.getInstance().collection("User").document(MainActivity.currentUser.getId())
                        .update("signIn", false)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                FirebaseAuth.getInstance().signOut();
                                finishAffinity();
                                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                                finish();
                            }
                        });
            }
        });

        binding.changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sweetAlertDialog = new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                sweetAlertDialog.setCancelable(false);
                sweetAlertDialog.show();
                updatePassword();
            }
        });

        binding.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImage.launch("image/*");
            }
        });

        setContentView(binding.getRoot());
    }

    private void updatePassword() {
        String password = binding.changedPassword.getText().toString();
        String cf_password = binding.changedPasswordCf.getText().toString();
        if (password.length() == 0) {
            binding.changedPassword.setError("Empty");
            return;
        }
        if (cf_password.length() == 0) {
            binding.changedPasswordCf.setError("Empty");
            return;
        }
        if (!password.equals(cf_password)) {
            binding.changedPasswordCf.setError("Password does not match!");
            return;
        }
        FirebaseAuth.getInstance().getCurrentUser().updatePassword(password)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ProfileActivity.this, "Password update!", Toast.LENGTH_SHORT).show();
                        sweetAlertDialog.dismissWithAnimation();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        sweetAlertDialog.dismissWithAnimation();
                    }
                });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.profile_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (R.id.saveChanges_button == id) {
            String name = binding.userName.getText().toString();
            String number = binding.userNumber.getText().toString();
            if (name.length() == 0) {
                binding.userName.setError("Empty");
                return false;
            }
            if (number.length() == 0) {
                binding.userNumber.setError("Empty");
                return false;
            }
            if (tempImage != null) {
                StorageReference reference = storage.getReference().child("images/" + tempUser.getId());
                reference.putFile(tempImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d("FinancialApp", "Upload profile picture successfully!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FinancialApp", "Upload profile picture failed!");
                    }
                });
            }
            tempUser.setName(name);
            tempUser.setNumber(number);
            FirebaseFirestore.getInstance().collection("User").document(tempUser.getId())
                    .set(tempUser)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(ProfileActivity.this, "Update successfully!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileActivity.this, "Update failed!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                            finish();
                        }
                    });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}