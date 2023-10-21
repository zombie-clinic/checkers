insert into player (id, name) values (1, 'Alice');
insert into player (id, name) values (2, 'Bob');

insert into game (id, player_id, progress) values ('0a83d555-abfe-42f9-8d46-0d23d7a1d863', 2,'STARTING');
insert into game (id, player_id, progress) values ('1f3bea4e-48e3-4b7b-8ff8-6287f201b510', 2,'STARTING');
insert into game (id, player_id, progress) values ('7304942c-1bfd-4c23-8c83-c9902a866807', 2,'ONGOING');
insert into game (id, player_id, progress) values ('cb3e0675-3689-4d7f-b5d6-1f3c19b657c9', 2,'ONGOING');
insert into game (id, player_id, progress) values ('5525d529-9bb5-489a-b61b-2397962f9d6e', 2,'COMPLETED');
insert into game (id, player_id, progress) values ('0e59c559-b0ef-4345-8c05-40a267b8a8a3', 2,'COMPLETED');
insert into game (id, player_id, progress) values ('79d401ab-ac28-4f27-8c42-ee907c886d74', 1,'COMPLETED');
insert into game (id, player_id, progress) values ('0f71e103-2278-4a75-b5c0-e555f859fdac', 2,'COMPLETED');
insert into game (id, player_id, progress) values ('12157ca4-fb32-4b66-baa8-c73c54593fcd', 2,'ARCHIVED');
insert into game (id, player_id, progress) values ('4a639f32-7555-4a7d-82a7-c3a8613f5b7f', 2,'ARCHIVED');

--will need to move to array column type when change to postgresql
insert into move (game_id, player_id, side, move, black, white) values ('7304942c-1bfd-4c23-8c83-c9902a866807', 1, 'white', '22-18', '1,2,3,4,5,6,7,8,9,10,11,12','21,23,24,25,26,27,28,29,30,31,32,18');
insert into move (game_id, player_id, side, move, black, white) values ('7304942c-1bfd-4c23-8c83-c9902a866807', 2, 'black', '10-14', '1,2,3,4,5,6,7,8,9,11,12,14','21,23,24,25,26,27,28,29,30,31,32,18');