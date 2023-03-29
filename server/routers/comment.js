import express from "express";
import auth from "../middleware/auth.js";
import Comment from "../models/comment.js";
import Post from "../models/post.js";

const router = express.Router();


router.post('/post' , async (req, res) => {

    try {
        const {userId ,content , postId   } = req.body ;

    const newComment = await Comment.create({
        owner : userId,
        content
    })

     await Post.findByIdAndUpdate(postId , {$push:{comments : newComment._id}} , {new:true});

    const populatedComment = await Comment.findById(newComment._id).populate({
        path: 'owner',
        select: 'username avatarUrl'
      })


    res.status(200).json(populatedComment);

    } catch (error) {

        res.status(404).send(error)
    }




})

router.get('/get/:id' , async (req, res) => {

    try {
        const {id} = req.params ;

        const post = await Post.findById(id).populate({ 
            path: 'comments',
            populate: {
              path: 'owner',
              select: 'username avatarUrl' // you can specify the fields you want to retrieve from the owner model
            }
          })

        res.status(200).json(post.comments.reverse());

    } catch (error) {

        res.status(404).send(error)
    }

})







export default router