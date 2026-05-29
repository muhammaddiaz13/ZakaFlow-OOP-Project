#!/bin/bash

# Pastikan TIDAK ADA SPASI di sekitar tanda sama dengan (=)
export DB_URL="jdbc:postgresql://db.szvoiuccuwcbguchvfxm.supabase.co:5432/postgres?user=postgres&password=Willyam13042006//"
export DB_USERNAME="postgres"
export DB_PASSWORD="Willyam13042006//"

# Jalankan Spring Boot
./mvnw spring-boot:run