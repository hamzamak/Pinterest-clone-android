import express  from "express";
import mongoose from "mongoose";
import {config} from 'dotenv'
import userRoutes from './routers/user.js'
import postRoutes from './routers/post.js'
import commentRoutes from './routers/comment.js' ;
import morgan from "morgan";
config();
 import  cors from 'cors'

 const port = 5000

const app = express();

app.use(express.json({limit : '500mb'}))

 app.use(cors())
app.use(morgan('tiny')) 
app.use('/api/v1/users' , userRoutes);
app.use('/api/v1/posts' , postRoutes);
app.use('/api/v1/comments' , commentRoutes);



app.listen(port , ()=> {
    mongoose.set('strictQuery', false);
    mongoose.connect(process.env.MONGODB_URI , ()=> {
        console.log(`mongodb connected successefully `)
    })
    console.log(`listening on port ${port}`);
}
);