package com.eugen.sandbox.jpa.h2.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.eugen.sandbox.grpc.CreateTutorialServiceGrpc;
import com.eugen.sandbox.grpc.Input;
import com.eugen.sandbox.grpc.Response;
import com.eugen.sandbox.grpc.ResponseType;
import com.eugen.sandbox.jpa.h2.dto.ResponseDTO;
import com.eugen.sandbox.jpa.h2.repository.TutorialRepository;
import io.grpc.CallCredentials;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eugen.sandbox.jpa.h2.model.Tutorial;

import static java.lang.Math.toIntExact;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class TutorialController {

	@Autowired
    TutorialRepository tutorialRepository;

	@PostMapping("/grpccreatesingletutorial")
	public ResponseEntity<ResponseDTO> testGrpcCreate(@RequestBody Tutorial tutorial){
		try {
			ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
					.usePlaintext()
					.build();
			CreateTutorialServiceGrpc.CreateTutorialServiceBlockingStub stub = CreateTutorialServiceGrpc.newBlockingStub(channel);

			Response response = stub.create(com.eugen.sandbox.grpc.Tutorial.newBuilder()
					.setDescription(tutorial.getDescription())
					.setPublished(tutorial.isPublished())
					.setTitle(tutorial.getTitle())
					.build());

			return new ResponseEntity<>(
					new ResponseDTO(response.getCreatedTutorials(), response.getNotCreatedTutorials(), response.getElapsedTime(), response.getResponse()),
					HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(
					new ResponseDTO(0, 0, 0, "ERROR"),
					HttpStatus.INTERNAL_SERVER_ERROR
			);
		}
	}

	@GetMapping("/grpctutorials")
	public ResponseEntity<List<Tutorial>> getAllTutorialsWithGRPC(@RequestParam(required = false) String title) {
		try{
			ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
					.usePlaintext()
					.build();

			title = title==null?"":title;
			Input input = Input.newBuilder()
					.setSearchString(title)
					.build();
			CreateTutorialServiceGrpc.CreateTutorialServiceBlockingStub stub = CreateTutorialServiceGrpc.newBlockingStub(channel);

			Iterator<com.eugen.sandbox.grpc.Tutorial> tutorialIterator = stub.listAllTutorials(input);

			List<Tutorial> response = new ArrayList<>();

			tutorialIterator.forEachRemaining(tutorial -> response.add(new Tutorial(tutorial.getId(), tutorial.getTitle(), tutorial.getDescription(), tutorial.getPublished())));

			return new ResponseEntity<>(response, HttpStatus.OK);
		}catch (Exception e){
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/grpccreatemultipletutorial")
	public ResponseEntity<ResponseDTO> createAllTutorialsWithGRPC(@RequestBody List<Tutorial> tutorials){
		List<Response> responseAll = new ArrayList<>();
		long startTime = System.nanoTime();
		try{
			ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
					.usePlaintext()
					.build();
			CreateTutorialServiceGrpc.CreateTutorialServiceStub stub = CreateTutorialServiceGrpc.newStub(channel);

			StreamObserver<com.eugen.sandbox.grpc.Tutorial> tutorialStreamObserver = stub.createTutorials(new StreamObserver<Response>() {
				@Override
				public void onNext(Response response) {
					responseAll.add(response);
					System.out.println("next");
				}

				@Override
				public void onError(Throwable throwable) {
					// elaborate top notch error handling
					System.out.println(throwable.getMessage());
				}

				@Override
				public void onCompleted() {
					System.out.println("complete");
				}
			});
			tutorials.forEach(
					tutorial -> tutorialStreamObserver.onNext(
							com.eugen.sandbox.grpc.Tutorial.newBuilder()
									.setTitle(tutorial.getTitle())
									.setPublished(tutorial.isPublished())
									.setDescription(tutorial.getDescription())
									.build()));

			int notCreatedTutorials = responseAll.stream().mapToInt(Response::getNotCreatedTutorials).sum();
			int createdTutorials = responseAll.stream().mapToInt(Response::getCreatedTutorials).sum();
			long seconds = NANOSECONDS.toSeconds(System.nanoTime() - startTime);
			return new ResponseEntity<>(
					new ResponseDTO(createdTutorials, notCreatedTutorials, toIntExact(seconds), "OK"),
					HttpStatus.OK);
		}catch (Exception e){
			long seconds = NANOSECONDS.toSeconds(System.nanoTime() - startTime);
			return new ResponseEntity<>(
					new ResponseDTO(0, tutorials.size(), toIntExact(seconds), "ERROR"),
					HttpStatus.INTERNAL_SERVER_ERROR
			);
		}
	}


	@PostMapping("/grpcvalidate")
	public ResponseEntity<ResponseDTO> validateAllTutorials(@RequestBody List<Tutorial> tutorials){
		List<Response> responseAll = new ArrayList<>();
		long startTime = System.nanoTime();
		try{
			ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
					.usePlaintext()
					.build();
			CreateTutorialServiceGrpc.CreateTutorialServiceStub stub = CreateTutorialServiceGrpc.newStub(channel);

			StreamObserver<com.eugen.sandbox.grpc.Tutorial> tutorialStreamObserver = stub.validateTutorials(new StreamObserver<com.eugen.sandbox.grpc.Tutorial>() {
				@Override
				public void onNext(com.eugen.sandbox.grpc.Tutorial tutorial) {
					if(tutorial==null){
						System.out.println("received nothing");
					}else{
						System.out.println("recieved tutorial"+tutorial.getTitle());
					}
				}

				@Override
				public void onError(Throwable throwable) {
					// elaborate top notch error handling
					System.out.println(throwable.getMessage());
				}

				@Override
				public void onCompleted() {
					System.out.println("completed");
				}
			});
			tutorials.forEach(
					tutorial -> tutorialStreamObserver.onNext(
							com.eugen.sandbox.grpc.Tutorial.newBuilder()
									.setTitle(tutorial.getTitle())
									.setPublished(tutorial.isPublished())
									.setDescription(tutorial.getDescription())
									.build()));

			long seconds = NANOSECONDS.toSeconds(System.nanoTime() - startTime);
			return new ResponseEntity<>(
					new ResponseDTO(0, 0, toIntExact(seconds), "OK"),
					HttpStatus.OK);
		}catch (Exception e){
			long seconds = NANOSECONDS.toSeconds(System.nanoTime() - startTime);
			return new ResponseEntity<>(
					new ResponseDTO(0, tutorials.size(), toIntExact(seconds), "ERROR"),
					HttpStatus.INTERNAL_SERVER_ERROR
			);
		}
	}

	@PostMapping("/grpccreatesingletutorialauthorized")
	public ResponseEntity<ResponseDTO> testGrpcCreateAuthorized(@RequestBody Tutorial tutorial){
		try {
			ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
					.usePlaintext()
					.build();
			CreateTutorialServiceGrpc.CreateTutorialServiceBlockingStub stub = CreateTutorialServiceGrpc.newBlockingStub(channel);

			Response response = stub.create(com.eugen.sandbox.grpc.Tutorial.newBuilder()
					.setDescription(tutorial.getDescription())
					.setPublished(tutorial.isPublished())
					.setTitle(tutorial.getTitle())
					.build());

			return new ResponseEntity<>(
					new ResponseDTO(response.getCreatedTutorials(), response.getNotCreatedTutorials(), response.getElapsedTime(), response.getResponse()),
					HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(
					new ResponseDTO(0, 0, 0, "ERROR"),
					HttpStatus.INTERNAL_SERVER_ERROR
			);
		}
	}

	@GetMapping("/tutorials")
	public ResponseEntity<List<Tutorial>> getAllTutorials(@RequestParam(required = false) String title) {
		try {
			List<Tutorial> tutorials = new ArrayList<Tutorial>();

			if (title == null)
				tutorialRepository.findAll().forEach(tutorials::add);
			else
				tutorialRepository.findByTitleContaining(title).forEach(tutorials::add);

			if (tutorials.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			return new ResponseEntity<>(tutorials, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/tutorials/{id}")
	public ResponseEntity<Tutorial> getTutorialById(@PathVariable("id") long id) {
		Optional<Tutorial> tutorialData = tutorialRepository.findById(id);

		if (tutorialData.isPresent()) {
			return new ResponseEntity<>(tutorialData.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping("/tutorials")
	public ResponseEntity<Tutorial> createTutorial(@RequestBody Tutorial tutorial) {
		try {
			Tutorial _tutorial = tutorialRepository
					.save(new Tutorial(tutorial.getTitle(), tutorial.getDescription(), false));
			return new ResponseEntity<>(_tutorial, HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/tutorials/{id}")
	public ResponseEntity<Tutorial> updateTutorial(@PathVariable("id") long id, @RequestBody Tutorial tutorial) {
		Optional<Tutorial> tutorialData = tutorialRepository.findById(id);

		if (tutorialData.isPresent()) {
			Tutorial _tutorial = tutorialData.get();
			_tutorial.setTitle(tutorial.getTitle());
			_tutorial.setDescription(tutorial.getDescription());
			_tutorial.setPublished(tutorial.isPublished());
			return new ResponseEntity<>(tutorialRepository.save(_tutorial), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@DeleteMapping("/tutorials/{id}")
	public ResponseEntity<HttpStatus> deleteTutorial(@PathVariable("id") long id) {
		try {
			tutorialRepository.deleteById(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/tutorials")
	public ResponseEntity<HttpStatus> deleteAllTutorials() {
		try {
			tutorialRepository.deleteAll();
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/tutorials/published")
	public ResponseEntity<List<Tutorial>> findByPublished() {
		try {
			List<Tutorial> tutorials = tutorialRepository.findByPublished(true);

			if (tutorials.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(tutorials, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
